.PHONY: run
run: target
	java -cp target/allocine-light-1.0-SNAPSHOT-jar-with-dependencies.jar Runner --dry-run

.PHONY: deploy
deploy: clean target
	aws lambda update-function-code --cli-connect-timeout 6000 --region eu-west-3 --function-name allocine-light --zip-file fileb://target/allocine-light-1.0-SNAPSHOT-jar-with-dependencies.jar
	aws lambda invoke --cli-binary-format raw-in-base64-out --function-name allocine-light --payload '{"key1": "value1"}' response.json

.PHONY: build
build: target

target:
	mvn package

.PHONY: clean
clean:
	rm -rf target
	rm -rf www
	rm -f response.json
