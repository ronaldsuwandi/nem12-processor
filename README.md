# nem12-processor
## Background
The aim for this project is to read nem12 file and write the output into database. More details 
is within `doc` directory - it contains both the assignment and the nem12 standard

### Schema
Database schema is stored in `sql/schema.sql` which is automatically run when docker is created. 

The schema is slightly different from the assignment because there is a unique constraint, 
so `register_id` is added as part of the column so that the meter reading can be more granular 

### Prerequisites
Docker is optional but recommended

In order to run the application, you'll need an existing Postgres database. There is 
`docker-compose.yml` file provided for the convenience. Simply run `docker compose up -d` 
to spin up the database and to shut down remember to remove the volume `docker compose down -v`.

This will make sure that next time docker compose is run it will run with a fresh empty database

### NEM12 generator
NodeJS is only required to run the NodeJS script under `scripts` directory.

For convenience, there is a simple nodejs script under `scripts/generateNem12File.js` to generate 
the some random nem12 file, using the script you can then generate a large nem12 file easily 

### To run the code
` ./gradlew run --args='<path-to-nem12-file>'`

or optionally include additional file for the config

` ./gradlew run --args='<path-to-nem12-file> <path-to-config.properties>'`

To confirm if application is working as expected, check the database 

```sql
select count(1) from meter_readings;
select * from meter_readings LIMIT 100;
```

### To test the code
`./gradlew test`

## Scope
The scope for the project is mainly to handle record 100, 200, 300 and 900. The 
application itself reads 400 and 500 record but perform no further validation as 
it's out of scope

## Dependencies
- Not to use Spring Boot to keep the application light weight and minimise dependencies
- Uses HikariCP for connection pool
- Apache-commons for both config and CSV parsing
- Database simply uses PreparedStatement. There is no need for ORM. Using ORM will add
  unnecessary overhead

## Architecture
The application is implemented using single producer and multiple consumers approach
with LinkedBlockingQueue act as the queue in between

1. When application starts, it first obtain config and setup database connection pool
2. It then perform 2-pass file loading while maintaining file lock. Where the 
   first pass it simply read the input file and verify if the record is all good
3. The application will then perform second pass where it reads the file and push the data
   into the queue on a separate thread. By this time we are confirmed that file is valid 
   and we maintain the lock so there is no concern of file is being modified
4. Another consumer threads then runs to consume the queue and push the record into 
   database respectively

When processing the file it performs the following
```java
preProcess.preProcessXXX(state, record);
processXXX(record);
postProcess.postProcessXXX(state);
```

- Where `NEM12PreProcess` is implemented by `NEM12Validation`. The main job is to 
  validate the entry, ensure it's parseable and when validating entry 300, to ensure 
  that entry 200 is already processed correctly
- `NEM12PostProcess` is then where it perform post processing function.
- In the current logic for the first pass `NEM12Validation` is used for the preProcess 
  and `NoopPrePostProcess` is used for post-processing since there is no need to 
  post-process during validation stage
- Whereas in the second pass `NEM12FileProcessor` itself isused for the postProcess and
  `NoopPrePostProcess` is used for the pre-processing stage. Since we already validated 
  the record, there is no need to do pre-processing. `NEM12FileProcessor` post-processing 
  only focuses on record 300 and what it does is to simply place the record in the queue
- The consumer then consume the queue and depending on `NEM12ProcessorOutput` implementation 
  it will call `write(OutputEntity)`. Currently only `NEM12PostgresOutput` is implementing 
  `NEM12ProcessorOutput` and the job is to write to database

## Decisions
Before deciding on the current approach, I had 3 options to implement this project

### Option 1: Validate the whole file and process them only if all good
This will be a 2 pass approach, first pass is just to read and process all the 
entries within the file, once we clarify that the file is clear, we proceed with 
writing to db. This is the implemented solution

Pros:
- data integrity
- no need for transaction

Cons:
- slightly slower - although not that much, reading 1GB input is pretty fast, less than few seconds
- more resource usage
- have to lock file to ensure data integrity

This assumes that locking the file will work and once we obtained the lock no other 
process can modify the file. This way we can ensure after the first pass is completed 
the file will remain the same and we can proceed with writing to the database

### Option 2: Read and process the database output using transaction
Create a transaction before writing to db, so whenever we encounter invalid 
entries halfway through the file, we can just abort the transaction

Pros: 
- simple to implement
- data integrity
- works well for smaller files

Cons:
- transaction locking can be an issue if processing large file. 

Using `src/test/resources/large_nem12_file.csv` it contains about 360k rows (90MB) 
and that file has 48x write amplification due to 30min interval, as a result it 
translated into 17 million writes and took about 4 minutes - if we process 
larger file, transaction might timed out

### Option 3: Read and just process them leaving some invalid record
Similar to option 2, but instead of failing the transaction, just stop the 
process and be done with it

Pros:
- simple
- no transaction locking issue

Cons:
- data integrity issue

This is another very simple approach but this raises issue with data integrity. 
If we need to clear up the data the process will be painful and potentially heavy 
on the database as well. Suppose we are using the same large file as described 
in Option 2, and only the record from 16 millionth is invalid, then we'll need 
to delete 16 million records from the database
