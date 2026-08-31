sed -i '/commons-lang3/d' build.gradle
sed -i "/spring-boot-starter-validation/a \ \ \ \ implementation 'io.minio:minio:8.5.7' // MinIO client" build.gradle
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew --stop
./gradlew compileJava 2>&1 | tail -20
