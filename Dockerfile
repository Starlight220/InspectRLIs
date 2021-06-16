FROM wpilib/ubuntu-base:18.04

COPY . /inspector/
RUN chmod +x /inspector/run.sh
RUN chmod +x /inspector/gradlew

ENTRYPOINT ["/inspector/run.sh"]
