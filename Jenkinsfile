pipeline {
    agent { label 'android-appium' }

    tools {
        jdk 'jdk-17'
        maven 'Maven-3.9'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        booleanParam(
            name: 'START_EMULATOR',
            defaultValue: false,
            description: 'Arrancar emulador en el pipeline (requiere AVD_NAME configurado en el agente)'
        )
        string(
            name: 'AVD_NAME',
            defaultValue: 'Pixel_7_API_34',
            description: 'Nombre del AVD si START_EMULATOR=true'
        )
    }

    environment {
        APPIUM_SERVER_URL = 'http://127.0.0.1:4723'
        ANDROID_DEVICE_NAME = 'emulator-5554'
        APP_PATH = "${WORKSPACE}/src/test/resources/app/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk"
        APK_URL = 'https://github.com/saucelabs/sample-app-mobile/releases/download/2.7.1/Android.SauceLabs.Mobile.Sample.app.2.7.1.apk'
        APPIUM_LOG = "${WORKSPACE}/target/appium.log"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare APK') {
            steps {
                sh '''
                    mkdir -p src/test/resources/app
                    if [ ! -f "$APP_PATH" ]; then
                        echo "Descargando APK demo de Sauce Labs..."
                        curl -fsSL "$APK_URL" -o "$APP_PATH"
                    else
                        echo "APK ya presente en $APP_PATH"
                    fi
                '''
            }
        }

        stage('Start Emulator') {
            when {
                expression { params.START_EMULATOR }
            }
            steps {
                sh '''
                    nohup emulator -avd "$AVD_NAME" -no-window -no-audio -no-boot-anim > target/emulator.log 2>&1 &
                    echo $! > target/emulator.pid
                '''
            }
        }

        stage('Start Appium') {
            steps {
                sh '''
                    mkdir -p target
                    nohup appium --address 127.0.0.1 --port 4723 --log "$APPIUM_LOG" > target/appium-start.log 2>&1 &
                    echo $! > target/appium.pid

                    echo "Esperando Appium en ${APPIUM_SERVER_URL}..."
                    for i in $(seq 1 30); do
                        if curl -fsS "${APPIUM_SERVER_URL}/status" > /dev/null 2>&1; then
                            echo "Appium listo."
                            exit 0
                        fi
                        sleep 2
                    done

                    echo "Appium no respondió a tiempo."
                    exit 1
                '''
            }
        }

        stage('Wait for Device') {
            steps {
                sh '''
                    adb wait-for-device

                    echo "Esperando boot completo del dispositivo..."
                    boot_completed=""
                    for i in $(seq 1 60); do
                        boot_completed=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
                        if [ "$boot_completed" = "1" ]; then
                            echo "Dispositivo listo: $(adb devices -l)"
                            exit 0
                        fi
                        sleep 5
                    done

                    echo "El dispositivo no terminó de arrancar."
                    adb devices -l
                    exit 1
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
            archiveArtifacts artifacts: 'target/reports/**', allowEmptyArchive: true
            publishHTML(target: [
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target/reports',
                reportFiles: 'ExtentReport.html',
                reportName: 'Extent Report'
            ])
            script {
                sh '''
                    if [ -f target/appium.pid ]; then
                        kill "$(cat target/appium.pid)" 2>/dev/null || true
                        rm -f target/appium.pid
                    fi
                '''
                if (params.START_EMULATOR) {
                    sh '''
                        if [ -f target/emulator.pid ]; then
                            kill "$(cat target/emulator.pid)" 2>/dev/null || true
                            rm -f target/emulator.pid
                        fi
                    '''
                }
            }
        }
        failure {
            sh '''
                mkdir -p target/debug
                adb logcat -d > target/debug/logcat.txt 2>/dev/null || true
            '''
            archiveArtifacts artifacts: 'target/debug/**,target/appium.log,target/appium-start.log,target/emulator.log', allowEmptyArchive: true
        }
    }
}
