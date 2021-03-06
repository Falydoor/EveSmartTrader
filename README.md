# EveSmartTrader

A web application for the game EVE Online that shows you a list of trades you can do in the in-game market.

There is three kind of trades you can do :

* Hub trade : a trade between two stations of two different system. This trade is based on the sell order's price of both station.

* Station trade : a trade in one station, this trade is based on the price of the buy order's and the sell order's price.

* Penury trade : a trade when there is no sell order.

This application was generated using JHipster, you can find documentation and help at [https://jhipster.github.io](https://jhipster.github.io).

## Prerequisites

### Database

PostgreSQL must be installed and a database "EveSmartTrader" must be created.

### Configuration

Rename all the .yml files in src/main/resources/config/ by removing "-sample"

##### application.yml

Generate a key and replace "myKey" with it in jhipster.security.rememberMe.key

##### application-dev.yml

Configure your datasource credentials in spring.datasource

##### application-prod.yml

Configure your datasource credentials in spring.datasource

## Development

Before you can build this project, you must install and configure the following dependencies on your machine:

1. [Node.js][]: We use Node to run a development web server and build the project.
   Depending on your system, you can install Node either from source or as a pre-packaged bundle.

After installing Node, you should be able to run the following command to install development tools (like
[Bower][] and [BrowserSync][]). You will only need to run this command when dependencies change in package.json.

    npm install

We use [Gulp][] as our build system. Install the Gulp command-line tool globally with:

    npm install -g gulp

Run the following commands in two separate terminals to create a blissful development experience where your browser
auto-refreshes when files change on your hard drive.

    ./mvnw
    gulp

Bower is used to manage CSS and JavaScript dependencies used in this application. You can upgrade dependencies by
specifying a newer version in `bower.json`. You can also run `bower update` and `bower install` to manage dependencies.
Add the `-h` flag on any command to see how you can use it. For example, `bower update -h`.


## Building for production

To optimize the EveSmartTrader client for production, run:

    ./mvnw -Pprod clean package

This will concatenate and minify CSS and JavaScript files. It will also modify `index.html` so it references
these new files.

To ensure everything worked, run:

    java -jar target/*.war --spring.profiles.active=prod

Then navigate to [http://localhost:8080](http://localhost:8080) in your browser.
