sed -i '/io.minio/d' build.gradle
sed -i "/spring-boot-starter-validation/a \ \ \ \ implementation('io.minio:minio:8.4.3')" build.gradle
./gradlew compileJava
