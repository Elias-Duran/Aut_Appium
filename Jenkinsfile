
pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))

        // Evita que Jenkins haga un checkout automático
        // porque ya tenemos un stage Checkout.
        skipDefaultCheckout(true)

        // Evita que dos ejecuciones intenten usar
        // el mismo emulador y el mismo puerto de Appium.
        disableConcurrentBuilds()
    }

    environment {
        ANDROID_HOME = '/usr/lib/android-sdk'
        ANDROID_SDK_ROOT = '/usr/lib/android-sdk'
        ANDROID_AVD_HOME = '/var/lib/jenkins/.android/avd'

        PATH = "/usr/lib/android-sdk/platform-tools:/usr/lib/android-sdk/emulator:${env.PATH}"

        AVD_NAME = 'Pixel_7_API_34'
        EMULATOR_PORT = '5554'
        DEVICE_SERIAL = 'emulator-5554'

        APPIUM_SERVER_URL = 'http://127.0.0.1:4723'

        APP_PATH = "${WORKSPACE}/src/test/resources/app/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk"

        APK_URL = 'https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk'

        CI_DIR = "${WORKSPACE}/.ci"

        APPIUM_LOG = "${WORKSPACE}/.ci/appium.log"
        APPIUM_START_LOG = "${WORKSPACE}/.ci/appium-start.log"
        EMULATOR_LOG = "${WORKSPACE}/.ci/emulator.log"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Runtime') {
            steps {
                sh '''
                    mkdir -p "$CI_DIR"

                    rm -f "$CI_DIR/appium.pid"
                    rm -f "$CI_DIR/appium.started"
                    rm -f "$CI_DIR/emulator.started"

                    rm -f "$APPIUM_LOG"
                    rm -f "$APPIUM_START_LOG"
                    rm -f "$EMULATOR_LOG"

                    echo "Directorio temporal preparado:"
                    echo "$CI_DIR"
                '''
            }
        }

        stage('Validate Tools') {
            steps {
                sh '''
                    echo "===== JAVA ====="
                    java -version

                    echo "===== MAVEN ====="
                    mvn -version

                    echo "===== NODE ====="
                    node -v

                    echo "===== NPM ====="
                    npm -v

                    echo "===== APPIUM ====="
                    appium -v

                    echo "===== ADB ====="
                    adb version

                    echo "===== EMULATOR ====="
                    emulator -version

                    echo "===== AVD DISPONIBLES ====="
                    emulator -list-avds

                    if ! emulator -list-avds | grep -Fxq "$AVD_NAME"; then
                        echo "ERROR: No existe el AVD $AVD_NAME"
                        exit 1
                    fi

                    echo "===== KVM ====="

                    if [ ! -e /dev/kvm ]; then
                        echo "ERROR: No existe /dev/kvm"
                        exit 1
                    fi

                    if [ ! -r /dev/kvm ] || [ ! -w /dev/kvm ]; then
                        echo "ERROR: Jenkins no tiene permisos para usar KVM."
                        ls -l /dev/kvm || true
                        id
                        exit 1
                    fi

                    echo "Jenkins tiene acceso a KVM."
                    ls -l /dev/kvm
                    id
                '''
            }
        }

        stage('Prepare APK') {
            steps {
                sh '''
                    mkdir -p src/test/resources/app

                    if [ ! -f "$APP_PATH" ]; then
                        echo "Descargando APK demo..."

                        curl \
                            --fail \
                            --location \
                            --show-error \
                            "$APK_URL" \
                            --output "$APP_PATH"
                    else
                        echo "APK ya existe."
                    fi

                    if [ ! -s "$APP_PATH" ]; then
                        echo "ERROR: El APK no existe o está vacío."
                        exit 1
                    fi

                    echo "APK preparado:"
                    ls -lh "$APP_PATH"
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                sh '''
                    echo "Iniciando servidor ADB..."
                    adb start-server

                    echo "Estado inicial de los dispositivos:"
                    adb devices

                    DEVICE_STATE=$(adb -s "$DEVICE_SERIAL" get-state 2>/dev/null || true)
                    BOOT_STATUS=$(adb -s "$DEVICE_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r' || true)

                    if [ "$DEVICE_STATE" = "device" ] && [ "$BOOT_STATUS" = "1" ]; then
                        echo "El emulador ya está iniciado y disponible."
                    else
                        echo "El emulador no está listo."

                        if adb devices | grep -q "^${DEVICE_SERIAL}[[:space:]]*offline"; then
                            echo "Eliminando emulador offline..."

                            adb -s "$DEVICE_SERIAL" emu kill || true
                            sleep 5
                        fi

                        echo "Iniciando AVD: $AVD_NAME"

                        nohup emulator \
                            -avd "$AVD_NAME" \
                            -port "$EMULATOR_PORT" \
                            -no-window \
                            -no-audio \
                            -no-boot-anim \
                            -no-snapshot \
                            -no-metrics \
                            -gpu swiftshader_indirect \
                            > "$EMULATOR_LOG" 2>&1 &

                        echo $! > "$CI_DIR/emulator.pid"
                        touch "$CI_DIR/emulator.started"
                    fi

                    echo "Esperando que Android termine de iniciar..."

                    EMULATOR_READY=false

                    for i in $(seq 1 120); do
                        DEVICE_STATE=$(adb -s "$DEVICE_SERIAL" get-state 2>/dev/null || true)

                        BOOT_STATUS=$(adb -s "$DEVICE_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r' || true)

                        echo "Intento $i: estado=$DEVICE_STATE, boot=$BOOT_STATUS"

                        if [ "$DEVICE_STATE" = "device" ] && [ "$BOOT_STATUS" = "1" ]; then
                            EMULATOR_READY=true
                            break
                        fi

                        sleep 2
                    done

                    if [ "$EMULATOR_READY" != "true" ]; then
                        echo "ERROR: El emulador no terminó de iniciar."

                        echo "===== ADB DEVICES ====="
                        adb devices || true

                        echo "===== LOG DEL EMULADOR ====="
                        cat "$EMULATOR_LOG" || true

                        exit 1
                    fi

                    echo "Desbloqueando pantalla del emulador..."
                    adb -s "$DEVICE_SERIAL" shell input keyevent 82 || true

                    echo "Emulador iniciado correctamente."
                    adb devices
                '''
            }
        }

        stage('Check Device') {
            steps {
                sh '''
                    echo "===== DISPOSITIVOS ADB ====="
                    adb devices -l

                    DEVICE_STATE=$(adb -s "$DEVICE_SERIAL" get-state 2>/dev/null || true)

                    BOOT_STATUS=$(adb -s "$DEVICE_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r' || true)

                    if [ "$DEVICE_STATE" != "device" ]; then
                        echo "ERROR: $DEVICE_SERIAL no está conectado correctamente."
                        exit 1
                    fi

                    if [ "$BOOT_STATUS" != "1" ]; then
                        echo "ERROR: Android todavía no terminó de iniciar."
                        exit 1
                    fi

                    echo "Dispositivo Android disponible."
                    echo "Serial: $DEVICE_SERIAL"
                    echo "Estado: $DEVICE_STATE"
                    echo "Boot completado: $BOOT_STATUS"
                '''
            }
        }

        stage('Start Appium') {
            steps {
                sh '''
                    if curl -fsS "${APPIUM_SERVER_URL}/status" > /dev/null 2>&1; then
                        echo "Appium ya se encuentra ejecutándose."
                    else
                        echo "Iniciando Appium..."

                        nohup appium \
                            --address 127.0.0.1 \
                            --port 4723 \
                            --log "$APPIUM_LOG" \
                            > "$APPIUM_START_LOG" 2>&1 &

                        APPIUM_PID=$!

                        echo "$APPIUM_PID" > "$CI_DIR/appium.pid"
                        touch "$CI_DIR/appium.started"

                        APPIUM_READY=false

                        for i in $(seq 1 30); do
                            if curl -fsS "${APPIUM_SERVER_URL}/status" > /dev/null 2>&1; then
                                APPIUM_READY=true
                                break
                            fi

                            echo "Esperando Appium: intento $i"
                            sleep 2
                        done

                        if [ "$APPIUM_READY" != "true" ]; then
                            echo "ERROR: Appium no respondió."

                            echo "===== APPIUM START LOG ====="
                            cat "$APPIUM_START_LOG" || true

                            echo "===== APPIUM LOG ====="
                            cat "$APPIUM_LOG" || true

                            kill "$APPIUM_PID" 2>/dev/null || true

                            exit 1
                        fi
                    fi

                    echo "Appium listo."
                    curl -fsS "${APPIUM_SERVER_URL}/status"
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    echo "Ejecutando pruebas Maven..."

                    mvn \
                        --batch-mode \
                        --no-transfer-progress \
                        clean test
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "===== ESTADO FINAL DE ADB ====="
                adb devices || true

                echo "===== DETENIENDO APPIUM ====="

                if [ -f "$CI_DIR/appium.started" ] && [ -f "$CI_DIR/appium.pid" ]; then
                    APPIUM_PID=$(cat "$CI_DIR/appium.pid")

                    kill "$APPIUM_PID" 2>/dev/null || true

                    rm -f "$CI_DIR/appium.pid"
                    rm -f "$CI_DIR/appium.started"

                    echo "Appium detenido."
                else
                    echo "Este pipeline no inició Appium."
                fi

                echo "===== DETENIENDO EMULADOR ====="

                if [ -f "$CI_DIR/emulator.started" ]; then
                    adb -s "$DEVICE_SERIAL" emu kill || true

                    rm -f "$CI_DIR/emulator.started"
                    rm -f "$CI_DIR/emulator.pid"

                    echo "Emulador detenido."
                else
                    echo "Este pipeline no inició el emulador."
                fi

                echo "Limpieza finalizada."
            '''

            archiveArtifacts(
                artifacts: '.ci/*.log, target/surefire-reports/**/*',
                allowEmptyArchive: true
            )
        }

        success {
            echo 'Pipeline ejecutado correctamente.'
        }

        failure {
            echo 'Pipeline falló. Revisa los logs archivados de Appium, emulador y Maven.'
        }
    }
}

