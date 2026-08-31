sed -i '/software.amazon.awssdk/d' build.gradle
sed -i "/spring-boot-starter-validation/a \ \ \ \ implementation 'org.apache.commons:commons-lang3:3.14.0'" build.gradle
./gradlew compileJava
