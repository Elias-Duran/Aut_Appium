pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        ANDROID_HOME = '/usr/lib/android-sdk'
        ANDROID_SDK_ROOT = '/usr/lib/android-sdk'
        PATH = "/usr/lib/android-sdk/platform-tools:/usr/lib/android-sdk/emulator:${env.PATH}"

        APPIUM_SERVER_URL = 'http://127.0.0.1:4723'
        ANDROID_DEVICE_NAME = 'emulator-5554'

        APP_PATH = "${WORKSPACE}/src/test/resources/app/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk"
        APK_URL = 'https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk'

        APPIUM_LOG = "${WORKSPACE}/target/appium.log"
        EMULATOR_LOG = "${WORKSPACE}/target/emulator.log"
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
                    java -version
                    mvn -version
                    node -v
                    npm -v
                    appium -v
                    adb version
                    emulator -version
                '''
            }
        }

        stage('Prepare APK') {
            steps {
                sh '''
                    mkdir -p src/test/resources/app
                    if [ ! -f "$APP_PATH" ]; then
                        echo "Descargando APK demo..."
                        curl -fsSL "$APK_URL" -o "$APP_PATH"
                    else
                        echo "APK ya existe."
                    fi
                '''
            }
        }

        stage('Start Emulator') {
            steps {
                sh '''
                    mkdir -p target

                    adb kill-server || true
                    adb start-server

                    nohup emulator -avd Pixel_7_API_34 \
                        -no-window \
                        -no-audio \
                        -no-boot-anim \
                        -no-snapshot \
                        -no-metrics \
                        -gpu swiftshader_indirect > "$EMULATOR_LOG" 2>&1 &

                    echo $! > target/emulator.pid

                    echo "Esperando emulador..."
                    adb wait-for-device

                    echo "Esperando boot completo..."
                    for i in $(seq 1 60); do
                        boot_completed=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\\r')
                        if [ "$boot_completed" = "1" ]; then
                            echo "Emulador listo."
                            adb devices
                            exit 0
                        fi
                        sleep 5
                    done

                    echo "El emulador no terminó de iniciar."
                    adb devices
                    cat "$EMULATOR_LOG" || true
                    exit 1
                '''
            }
        }

        stage('Start Appium') {
            steps {
                sh '''
                    mkdir -p target

                    pkill -f appium || true

                    nohup appium --address 127.0.0.1 --port 4723 --log "$APPIUM_LOG" > target/appium-start.log 2>&1 &
                    echo $! > target/appium.pid

                    echo "Esperando Appium..."
                    for i in $(seq 1 30); do
                        if curl -fsS "$APPIUM_SERVER_URL/status" > /dev/null 2>&1; then
                            echo "Appium listo."
                            exit 0
                        fi
                        sleep 2
                    done

                    echo "Appium no respondió."
                    cat target/appium-start.log || true
                    exit 1
                '''
            }
        }

        stage('Check Device') {
            steps {
                sh '''
                    adb devices
                '''
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test -B'
            }
        }
    }

    post {
        always {
            sh '''
                if [ -f target/appium.pid ]; then
                    kill "$(cat target/appium.pid)" 2>/dev/null || true
                    rm -f target/appium.pid
                fi

                if [ -f target/emulator.pid ]; then
                    kill "$(cat target/emulator.pid)" 2>/dev/null || true
                    rm -f target/emulator.pid
                fi
            '''
        }
    }
}