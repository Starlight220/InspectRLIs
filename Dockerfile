FROM wpilib/ubuntu-base:18.04

COPY . /inspect_rli/
RUN chmod +x /inspect_rli/run.sh
RUN chmod +x /inspect_rli/gradlew

ENTRYPOINT ["/inspector/run.sh"]
