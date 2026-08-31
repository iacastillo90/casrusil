sed -i '/io.minio/d' build.gradle
sed -i "/spring-boot-starter-validation/a \ \ \ \ implementation 'software.amazon.awssdk:s3:2.25.11'" build.gradle
./gradlew compileJava
