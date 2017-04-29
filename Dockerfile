FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/swingstaterealestate.jar /swingstaterealestate/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/swingstaterealestate/app.jar"]
