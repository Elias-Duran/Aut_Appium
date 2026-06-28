pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    environment {
        ANDROID_HOME = '/usr/lib/android-sdk'
        ANDROID_SDK_ROOT = '/usr/lib/android-sdk'
        PATH = "/usr/lib/android-sdk/platform-tools:/usr/lib/android-sdk/emulator:/usr/lib/android-sdk/cmdline-tools/latest/bin:/usr/lib/android-sdk/build-tools/35.0.0:${env.PATH}"

        APPIUM_SERVER_URL = 'http://127.0.0.1:4723'
        APP_PATH = "${WORKSPACE}/src/test/resources/app/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk"
        APK_URL = 'https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk'

        APPIUM_LOG = "${WORKSPACE}/target/appium.log"
        EMULATOR_LOG = "${WORKSPACE}/target/emulator.log"

        AVD_NAME = 'Pixel_7_API_34'
        DEVICE_SERIAL = 'emulator-5554'
        EMULATOR_PORT = '5554'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Validate Tools') {
            steps {
                sh '''
                    set -eu

                    java -version
                    mvn -version
                    node -v
                    npm -v
                    appium -v
                    adb version
                    emulator -version

                    echo "ANDROID_HOME=$ANDROID_HOME"
                    echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"

                    find "$ANDROID_HOME/build-tools" -name aapt2 || true
                    emulator -list-avds
                    appium driver list --installed

                    if ! emulator -list-avds | grep -Fxq "$AVD_NAME"; then
                        echo "ERROR: El AVD '$AVD_NAME' no existe."
                        exit 1
                    fi
                '''
            }
        }

        stage('Clean Project') {
            steps {
                sh '''
                    set -eu
                    mvn clean -B
                    mkdir -p target
                '''
            }
        }

        stage('Prepare APK') {
            steps {
                sh '''
                    set -eu

                    mkdir -p "$(dirname "$APP_PATH")"

                    if [ ! -s "$APP_PATH" ]; then
                        echo "Descargando APK demo..."
                        rm -f "${APP_PATH}.tmp"

                        curl --fail --show-error --location "$APK_URL" --output "${APP_PATH}.tmp"

                        mv "${APP_PATH}.tmp" "$APP_PATH"
                    else
                        echo "La APK ya existe."
                    fi

                    ls -lh "$APP_PATH"
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                sh '''
                    set -eu

                    mkdir -p target

                    if adb devices | grep -q "^${DEVICE_SERIAL}[[:space:]]"; then
                        adb -s "$DEVICE_SERIAL" emu kill || true
                        sleep 5
                    fi

                    pkill -f "emulator.*-avd $AVD_NAME" || true

                    adb kill-server || true
                    adb start-server

                    ACCELERATION_ARGUMENT=""

                    if [ -r /dev/kvm ] && [ -w /dev/kvm ]; then
                        echo "KVM disponible."
                    else
                        echo "KVM no disponible para Jenkins. Usando -accel off."
                        ACCELERATION_ARGUMENT="-accel off"
                    fi

                    JENKINS_NODE_COOKIE=dontKillMe \
                    nohup emulator \
                        -avd "$AVD_NAME" \
                        -port "$EMULATOR_PORT" \
                        -no-window \
                        -no-audio \
                        -no-boot-anim \
                        -no-snapshot \
                        -no-metrics \
                        -gpu swiftshader_indirect \
                        $ACCELERATION_ARGUMENT \
                        </dev/null \
                        > "$EMULATOR_LOG" 2>&1 &

                    EMULATOR_PID=$!
                    echo "$EMULATOR_PID" > target/emulator.pid

                    DEVICE_FOUND=false

                    for i in $(seq 1 180); do
                        if adb devices | grep -q "^${DEVICE_SERIAL}[[:space:]]"; then
                            DEVICE_FOUND=true
                            break
                        fi

                        if ! kill -0 "$EMULATOR_PID" 2>/dev/null; then
                            echo "El emulador terminó inesperadamente."
                            cat "$EMULATOR_LOG" || true
                            exit 1
                        fi

                        echo "Esperando emulador... intento $i/180"
                        sleep 2
                    done

                    if [ "$DEVICE_FOUND" != "true" ]; then
                        echo "ADB no detectó el emulador."
                        adb devices || true
                        cat "$EMULATOR_LOG" || true
                        exit 1
                    fi

                    BOOT_COMPLETED=false

                    for i in $(seq 1 180); do
                        boot_completed=$(adb -s "$DEVICE_SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r')

                        if [ "$boot_completed" = "1" ]; then
                            BOOT_COMPLETED=true
                            break
                        fi

                        if ! kill -0 "$EMULATOR_PID" 2>/dev/null; then
                            echo "El emulador se cerró durante el arranque."
                            cat "$EMULATOR_LOG" || true
                            exit 1
                        fi

                        echo "Android arrancando... intento $i/180"
                        sleep 2
                    done

                    if [ "$BOOT_COMPLETED" != "true" ]; then
                        echo "Android no terminó de iniciar."
                        adb devices || true
                        cat "$EMULATOR_LOG" || true
                        exit 1
                    fi

                    adb -s "$DEVICE_SERIAL" shell input keyevent 82 || true
                    adb -s "$DEVICE_SERIAL" shell settings put global window_animation_scale 0 || true
                    adb -s "$DEVICE_SERIAL" shell settings put global transition_animation_scale 0 || true
                    adb -s "$DEVICE_SERIAL" shell settings put global animator_duration_scale 0 || true

                    adb devices -l
                '''
            }
        }

        stage('Start Appium') {
            steps {
                sh '''
                    set -eu

                    mkdir -p target

                    if [ -f target/appium.pid ]; then
                        OLD_APPIUM_PID=$(cat target/appium.pid)

                        if kill -0 "$OLD_APPIUM_PID" 2>/dev/null; then
                            kill "$OLD_APPIUM_PID" || true
                            sleep 2
                        fi

                        rm -f target/appium.pid
                    fi

                    pkill -f "appium.*4723" || true
                    sleep 2

                    JENKINS_NODE_COOKIE=dontKillMe \
                    nohup appium \
                        --address 127.0.0.1 \
                        --port 4723 \
                        --log "$APPIUM_LOG" \
                        </dev/null \
                        > target/appium-console.log 2>&1 &

                    APPIUM_PID=$!
                    echo "$APPIUM_PID" > target/appium.pid

                    for i in $(seq 1 60); do
                        if curl -fsS "${APPIUM_SERVER_URL}/status" > /dev/null 2>&1; then
                            echo "Appium está listo."
                            curl -fsS "${APPIUM_SERVER_URL}/status"
                            echo
                            exit 0
                        fi

                        if ! kill -0 "$APPIUM_PID" 2>/dev/null; then
                            echo "Appium terminó inesperadamente."
                            cat target/appium-console.log || true
                            cat "$APPIUM_LOG" || true
                            exit 1
                        fi

                        echo "Esperando Appium... intento $i/60"
                        sleep 2
                    done

                    echo "Appium no respondió dentro del tiempo esperado."
                    cat target/appium-console.log || true
                    cat "$APPIUM_LOG" || true
                    exit 1
                '''
            }
        }

        stage('Check Device') {
            steps {
                sh '''
                    set -eu

                    adb devices -l

                    if ! adb devices | grep -q "^${DEVICE_SERIAL}[[:space:]]*device$"; then
                        echo "ERROR: $DEVICE_SERIAL no está listo."
                        exit 1
                    fi

                    boot_completed=$(adb -s "$DEVICE_SERIAL" shell getprop sys.boot_completed | tr -d '\\r')

                    if [ "$boot_completed" != "1" ]; then
                        echo "ERROR: Android todavía no terminó de iniciar."
                        exit 1
                    fi
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    set -eu
                    mvn test -B
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "Cerrando servicios..."

                if [ -f target/appium.pid ]; then
                    APPIUM_PID=$(cat target/appium.pid)

                    if kill -0 "$APPIUM_PID" 2>/dev/null; then
                        kill "$APPIUM_PID" 2>/dev/null || true
                        sleep 2

                        if kill -0 "$APPIUM_PID" 2>/dev/null; then
                            kill -9 "$APPIUM_PID" 2>/dev/null || true
                        fi
                    fi

                    rm -f target/appium.pid
                fi

                if adb devices 2>/dev/null | grep -q "^${DEVICE_SERIAL}[[:space:]]"; then
                    adb -s "$DEVICE_SERIAL" emu kill || true
                    sleep 5
                fi

                if [ -f target/emulator.pid ]; then
                    EMULATOR_PID=$(cat target/emulator.pid)

                    if kill -0 "$EMULATOR_PID" 2>/dev/null; then
                        kill "$EMULATOR_PID" 2>/dev/null || true
                        sleep 2

                        if kill -0 "$EMULATOR_PID" 2>/dev/null; then
                            kill -9 "$EMULATOR_PID" 2>/dev/null || true
                        fi
                    fi

                    rm -f target/emulator.pid
                fi

                adb kill-server || true
            '''

            junit(
                testResults: 'target/surefire-reports/*.xml',
                allowEmptyResults: true
            )

            archiveArtifacts(
                artifacts: 'target/**/*.log,target/surefire-reports/**/*',
                allowEmptyArchive: true
            )
        }

        success {
            echo '✅ Pipeline ejecutado correctamente.'
        }

        failure {
            echo '❌ El pipeline falló. Revisa los logs archivados.'
        }
    }
}