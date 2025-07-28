# Summer Project: OurRainWater

[Jira Link](https://amateureconomist.atlassian.net/jira/software/projects/SPWATER/boards/37)
[Overleaf](https://www.overleaf.com/project/688606768e2edbd9845398a6)


To boot up the mock met office weather warnings, run `http-server -p 8002`.

You should be able to test our app by visiting [GitHub Pages](https://saquantum.github.io/summer_project/). Please do not perform any load testing since it is deployed on my own device :) 

Some test users are: 

{username: "admin", password: "admin"}

{username: "user_017", password: "123456"}

{username: "user_010", password: "123456"}

{username: "user_050", password: "123456"}

--------------

## Booting up

### Docker

Clone the main branch of this repository.
Make sure TCP port `80`, `8080` and `5432` are not occupied.

`cd` into the directory where this document lies at, and execute

```sh
docker compose up --build -d
```

You should see the following processes after execution:

```sh
# docker ps -a
CONTAINER ID   IMAGE                     COMMAND                   PORTS                                         NAMES
768ff03c38c4   summer_project-frontend   "/docker-entrypoint.…"    0.0.0.0:80->80/tcp, [::]:80->80/tcp           summer-frontend
8b4d8b2076d3   summer_project-backend    "sh -c 'java -jar ta…"    0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   summer-backend
b83b1d697200   postgis/postgis:15-3.3    "docker-entrypoint.s…"    0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   summer-database
```

Access port 80 in your browser. My linux VM is of IP `192.168.48.131` thus I access `http://192.168.48.131:80/`.

### Development

To run the project under development environment you will need to install Java JDK 17 and PostgreSQL beforehand.

Do install PostGis plugin during installation. Make sure the username of your PostgreSQL's root is 'postgres' which is the default username, and the password is '123456'. After installation create a new database dubbed 'rainwaterBD'.

If you have Java IDEs, open the `sp` directory in your IDE as Maven project and execute Maven's package lifecycle. You should see a `.jar` file in `sp/target` directory. 

If you don't have Java IDE, run `./mvnw clean package` (linux) or `mvnw.cmd clean package` (windows) in `sp/` directory where `mvnw` file exists. Likewise, you should be able to find the `.jar` file.

To boot up back-end service, direct to the `.jar` file and run `java -jar FILENAME.jar`. Replace the `FILENAME` placeholder to your actual file name.

To boot up front-end you need to first install `Node.js`, `npm` and `pnpm`. Once that is done, direct to `vue/` directory and run `pnpm install` and then run `pnpm dev`. 

When back-end and front-end are both ready, access `http://localhost:5173`.

### Document

A sketch documentation on the implementation of back-end service is visible at [here](document/backend.pdf).
