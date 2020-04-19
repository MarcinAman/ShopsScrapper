# Shop scrapper
A simple web scraper that periodically checks for daily deals on shops and sends the interesting ones to slack

## How it works
Every 5 minutes it checks sites in search of a deal. If a deal is found and matches the the parameters that user requested,
then it sends a slack notification. 

This project was primarly created to check how the new Akka Typed Api feels like :)

## Configuration
As for now the configuration is in the SQL Lite database, as it wasn't the main point to make a sophisticated REST / GQL API.
User has to insert values into `items` table which looks like:
```sql
create table items (
    id text,
    price bigint,
    username text
)
```

By default database is created in project directory and is named `scraper`.

Also it is important to fill the slack webhook token, if you want to have notifications sent that way.

## TODO-s
As for now it handles x-kom daily deals (Gorący strzał), al.to would be simple to add since it has same layout.
Also configuration can be improved to allow user to search for deals by description like "RefreshRate=144Hz".
