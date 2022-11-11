## Presentation

This is the code powering the website [cinelight.fr](). It gathers
all the movie-theaters programs for the day in Paris, France.

It is launched  by an AWS Lambda every night. It generates all the HTML files for 
[cinelight.fr]() and uploads them on the website server.

## Build and Run

Build and package with Maven

```
mvn clean package
```

You can then run with

```
java -cp target/allocine-light-1.0-SNAPSHOT-jar-with-dependencies.jar Runner 
```

The program will prompt you with
```
Dry run (Y/n) ?
```

If you answer Yes, it will output the HTML files locally in the `www` directory, 
otherwise you need the environment variables `CINE_HOST`, `CINE_LOGIN` 
and `CINE_PASSWORD` added to your environment and it will use these to upload
to your FTP server (the host must be a FTP host).

## Update AWS Lambda

I update my AWS Lambda with
```
aws lambda update-function-code --cli-connect-timeout 6000 --region eu-west-3 --function-name allocine-light --zip-file fileb://target/allocine-light-1.0-SNAPSHOT-jar-with-dependencies.jar
```