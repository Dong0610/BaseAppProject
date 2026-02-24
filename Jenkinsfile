pipeline {
  agent { label 'store-vohathi' }  // TODO: Change to store-name

  environment {
    // Use shared library for branch detection
    SOURCE_BRANCH = "${getSourceBranch()}"
    PRD_NAME = 'AS069' // TODO: Change to application code

    // Build type based on branch
    BUILD_TYPE = "${SOURCE_BRANCH == 'master' ? 'bundle' : 'apk'}"
    ARTIFACT_PATTERN = "${SOURCE_BRANCH == 'master' ? 'app/build/outputs/bundle/**/*.aab' : 'app/build/outputs/apk/**/*.apk'}"

    // Nexus repository settings
    NEXUS_URL = 'https://nexus.azuraglobal.vn'
    NEXUS_CRED = 'nexus-credentials'
    RAW_REPO = 'raw-android'

    // Play Console settings
    PLAY_TRACK = 'internal'

    // Discord notification
    DISCORD_CRED     = 'https://discord.com/api/webhooks/1467806189051187261/tAoDpCdtd-Cx0n2VYUpgdPv7MxmO0PSiySAvTmTyIj2tAwoNh2Q7aI71gzyUNBKRt3S0' // TODO: Change to discord channel webhookUrl
  }

  options {
    disableConcurrentBuilds()
    timestamps()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm

        // Create local.properties with SDK location
        powershell """
          \$androidHome = \$env:ANDROID_HOME
          if (-not \$androidHome) {
            Write-Host "ERROR: ANDROID_HOME environment variable is not set"
            exit 1
          }

          \$localProps = "\$env:WORKSPACE/local.properties"
          \$sdkPath = \$androidHome -replace '\\\\', '/'

          "sdk.dir=\$sdkPath" | Out-File -FilePath \$localProps -Encoding ASCII
          Write-Host "Created local.properties with sdk.dir=\$sdkPath"
        """

        script {
          // Use shared library for version extraction
          def versionInfo = extractVersionInfo()
          env.RELEASE_FOLDER = "/aab/${env.PRD_NAME}/${versionInfo.versionName}/${env.BUILD_NUMBER}"
        }

        echo "=== Build Environment Info ==="
        echo "SOURCE_BRANCH: ${env.SOURCE_BRANCH}"
        echo "ARTIFACT_PATTERN: ${env.ARTIFACT_PATTERN}"
        echo "VERSION_NAME: ${env.VERSION_NAME}"
        echo "VERSION_CODE: ${env.VERSION_CODE}"
        echo "APPLICATION_ID: ${env.APPLICATION_ID}"
        echo "RELEASE_FOLDER: ${env.RELEASE_FOLDER}"
        echo "PLAY_TRACK: ${env.PLAY_TRACK}"
        echo "=============================="
      }
    }

    stage('Prepare Credentials') {
      steps {
        // Copy gradle.properties from admin user to Jenkins workspace
        powershell """
          \$sourceGradleProps = "C:/Users/admin/.gradle/gradle.properties"
          \$destGradleDir = "\$env:WORKSPACE/.gradle"
          \$destGradleProps = "\$destGradleDir/gradle.properties"

          if (-not (Test-Path \$destGradleDir)) {
            New-Item -ItemType Directory -Path \$destGradleDir -Force | Out-Null
          }

          if (Test-Path \$sourceGradleProps) {
            Copy-Item -Path \$sourceGradleProps -Destination \$destGradleProps -Force
            Write-Host "Copied gradle.properties to \$destGradleProps"
          } else {
            Write-Host "ERROR: gradle.properties not found at \$sourceGradleProps"
            exit 1
          }
        """

        // Copy keystore from local storage to project directory
        powershell """
          \$keystoreSource = "C:/Users/admin/Desktop/keystore"
          \$keystoreDest = "\$env:WORKSPACE/keystore"

          if (-not (Test-Path \$keystoreDest)) {
            New-Item -ItemType Directory -Path \$keystoreDest -Force | Out-Null
          }

          if (Test-Path \$keystoreSource) {
            \$jksFiles = Get-ChildItem -Path \$keystoreSource -Filter "*.jks"
            if (\$jksFiles.Count -gt 0) {
              Copy-Item -Path "\$keystoreSource/*.jks" -Destination \$keystoreDest -Force
              Write-Host "Copied \$(\$jksFiles.Count) keystore file(s) to \$keystoreDest"
              Get-ChildItem \$keystoreDest -Filter "*.jks" | ForEach-Object { Write-Host "  - \$(\$_.Name)" }
            } else {
              Write-Host "WARNING: No .jks files found in \$keystoreSource"
            }
          } else {
            Write-Host "WARNING: Keystore source directory not found: \$keystoreSource"
          }
        """

        // Copy cpp folder from secrets to app directory
        script {
          def cppSource = "C:/jenkins/secrets/${env.PRD_NAME}/cpp"
          powershell """
            \$cppSource = "${cppSource}"
            \$cppDest = "\$env:WORKSPACE/app/cpp"

            if (Test-Path \$cppSource) {
              if (Test-Path \$cppDest) {
                Remove-Item -Path \$cppDest -Recurse -Force
              }

              Copy-Item -Path \$cppSource -Destination "\$cppDest" -Recurse -Force
              Write-Host "Copied cpp folder to app/"
            }
          """
        }
      }
    }

    stage('Build APK') {
      when { branch 'staging' }
      steps {
        // Use shared library for Gradle build with retry
        gradleBuild(
          task: 'app:assembleRelease',
          extraFlags: '-PFirebaseCrashlyticsMappingFileUploadEnabled=false'
        )
      }
    }

    stage('Build AAB') {
      when { branch 'master' }
      steps {
        // Use shared library for Gradle build with retry
        gradleBuild(task: 'app:bundleProductRelease')
      }
    }

    stage('Publish to Play Console') {
      when { branch 'master' }
      steps {
        // Use shared library for Play Console publishing
        publishToPlayConsole(
          serviceAccountPath: 'C:\\jenkins\\my-gp-account.json',
          gradleTask: 'publishProductReleaseBundle'
        )
      }
    }

    stage('Download APK from Play Console') {
      when { branch 'master' }
      steps {
        script {
          // Use shared library to download universal APK from Play Console
          def apkPath = downloadPlayConsoleApk(
            serviceAccountPath: 'C:\\jenkins\\my-gp-account.json',
            applicationId: env.APPLICATION_ID,
            versionCode: env.VERSION_CODE
          )
          env.DOWNLOADED_APK_PATH = apkPath
        }
      }
    }

    stage('Upload to Nexus') {
      steps {
        script {
          def artifactPaths = [:]

          if (env.SOURCE_BRANCH == 'master') {
            artifactPaths['playstore'] = env.DOWNLOADED_APK_PATH
          } else {
            // Staging branch: upload locally built APKs
            def artifacts = findFiles(glob: env.ARTIFACT_PATTERN)
            if (artifacts.length == 0) {
              echo "No ${env.BUILD_TYPE.toUpperCase()} files found to publish."
              return
            }
            artifacts.eachWithIndex { artifact, index ->
              def artifactPath = artifact.path.replace('\\', '/')
              artifactPaths["release-${index}"] = artifactPath
            }
          }

          def uploadResult = uploadApks(
            paths: artifactPaths,
            nexusUrl: env.NEXUS_URL,
            nexusRepo: env.RAW_REPO,
            productName: env.PRD_NAME,
            versionName: env.VERSION_NAME,
            nexusCredId: env.NEXUS_CRED
          )
          env.OUTPUT_LINKS = uploadResult.join('\n')
        }
      }
    }

    stage('Discord Notification') {
      steps {
        discordNotify(
          webhookUrl: env.DISCORD_CRED,
          outputLinks: env.OUTPUT_LINKS
        )
      }
    }
  }

  post {
    failure {
      // Use shared library for failure notification
      discordNotify(webhookUrl: env.DISCORD_CRED, type: 'failure')
    }
  }
}
