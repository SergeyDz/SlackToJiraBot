FROM maven:3.3.9-jdk-8-onbuild
COPY . /usr/src/bot
WORKDIR /usr/src/bot
RUN mvn package
ENV slackkey
ENV jirauser
ENV jirapassword
slackchannels
CMD ["java", "-jar", "target/SlackToJiraBot-1.0-jar-with-dependencies.jar"]
