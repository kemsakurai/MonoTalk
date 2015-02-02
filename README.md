
# MonoTalk
[![Build Status](https://travis-ci.org/kemsakurai/MonoTalk.svg)](https://travis-ci.org/kemsakurai/MonoTalk)
MonoTalk is an ORM library that fits Android ContentProvider's interface  

This Library baseed On 
[Active Android](https://github.com/pardom/ActiveAndroid), [Ollie](https://github.com/pardom/ollie/), [Sprinkles](https://github.com/emilsjolander/sprinkles),
[DBFlow](https://github.com/Raizlabs/DBFlow),[greenDAO](http://greendao-orm.com),  
and J2EE ORM framework [DBFlute](http://dbflute.seasar.org),[S2JDBC](http://s2container.seasar.http://www.seasar.org/en/)

## Functional Overview
- You can create the database tables by @Table annotation in the Entity class.
- You can create multiple database for single application
- You can Simple CRUD by EnityManager's method such as <code>insert(); update();</code>
- You can create advanced queries by QueryObject <code>manager.newSelect(); </code>
- You can create Query which beyond QueryObject's spec by 2waySql


## Installing
Add the maven repo url to your build.gradle:

    repositories {
          maven { url 'https://kemsakurai.github.io/maven/master/releases' }
    }

Add the library to the module-level or project-level build.gradle
    
    dependencies {
         compile 'MonoTalk:monotalk-db:0.0.1'
    }
    
## Configuration

Config Database and call <code>Monotalk.init()</code> in Your Application class

```java
	public class SampleApplication extends android.app.Application {
	    
		@Override
    	public void onCreate() {
        	super.onCreate();
            
	        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
			builder.setDataBaseName("NotesDB");
        	builder.setVersion(1);
			// default value is true
        	builder.setDefalutDatabase(true);
			// Add Entity Classes
			builder.addTable(Notes.class);
        	builder.addTable(NoteTag.class);
        	builder.addTable(Tag.class);
			// initialize
        	MonoTalk.init(getApplicationContext(), builder.create());
		}
        
		@Override
	    public void onTerminate() {
        	super.onTerminate();
	        MonoTalk.dispose();
    	}
	}
```

## Entities

- Extend Entity for all the classes that you need persisted.
- your class must be annotated using @Table
- your members must be annotated using @Column
- "_id" PK column is Created by framework in accordance with android contentprovider guidelines

```java
	@Table(name = "NOTES")
    public class Notes extends Entity {
        public static final String TITLE = "TITLE";
        public static final String BODY = "BODY";
        public static final String DATE = "DATE";

        @Column(name = TITLE)
        public String title;

        @Column(name = BODY)
        @NotNull
        public String body;

        @Column(name = DATE)
        public Date date;
    }
```
## CRUD DATABASE

#### Simple CRUD
INSERT  

```java
	EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    notes.title = "Title1";
    notes.body = "Hello World1";
    long id = manager.insert(notes);

	// ## YOU can excludes null members using manager#insertExcludesNull()
    Notes notes = new Notes();
    notes.title = null;
    notes.body = "Hello World1";
    long id = manager.insertExcludesNull(notes);
```
UPDATE  

```java
	EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
	Notes notes = new Notes();
    long id = 111l;
    notes.id = id;
    notes.title = "Title1";
    notes.body = "Hello World1";
    manager.update(notes);

	// YOU can excludes Null members using manager#upfateExcludesNull()
	Notes notes = new Notes();
    long id = 111l;
    notes.id = id;
    notes.title = null;
    notes.body = "Hello World1";
    manager.updateExcludesNull(notes);
	
    // You can update using SQLiteDatabase-like interface
    ContentValues value = new ContentValues();
    value.put(Notes.TITLE, "TEST");
    manager.updateById(Notes.class, value, 1l);
```

DELETE
```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    long id = 111l;
    notes.id = id;
    manager.delete(notes);

    // using Entity class and id
    manager.deleteById(Notes.class, 111l);
    // using Entity class and Selection String and bindParams
    manager.delete(Notes.class, "_id=?", 111l);
    // using SQLiteDatabase-like interface
    manager.delete("NOTES",  "_id=?", 111l);	
```

### QueryObject CRUD

You can create QueryObject by method of EntityManager which start with the "new" prefix

INSERT
```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    manager.newInsertInto(Notes.class).value(Notes.TITLE, "TEST").execute();
	
    // static import monotalk.db.query.QueryUtils#from()
    // You can create ContentsValue from Entity
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    notes.body = "Hello World1";
    // Method "values"'s  second parameter is includesColumns
    manager.newInsert(Notes.class).values(from(notes), "TITLE").execute();
	
```
UPDATE
```java
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        manager.newUpdate(Notes.class).as("Notes")
                .value("TITLE", "Hello")
                .where("_id").eq(111l);
        
        Notes notes = new Notes();
        notes.id = 100l;
        // static import monotalk.db.query.QueryUtils#idEquals() 
        manager.newUpdate(Notes.class).as("Notes")
                .value("TITLE", "Hello")
                .where(idEquals(notes));
       	
```
DELETE
```java
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        //  delete from notes where _id = 111;
        manager.newDelete(Notes.class)
               .where("_id").eq(111l);
        //  delete from notes where _id in (11,22,33);
        manager.newDelete(Notes.class)
               .where("_id").in(11l,22l,33l);
        //  delete from notes where DATE < "date.getTime();"
        manager.newDelete(Notes.class)
               .where("DATE").lt(new Date());

```
SELECT
```java 
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        
        // as Cursor
        Cursor cursor = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .limit(2)
                .offset(3).selectCursor();

        // as One Entity
        Notes notes = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectOne();

        // as EntityList
        List<Notes> noteList = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectList();

        // as LazyList
        LazyList<Notes> noteList = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectLazyList();

        // as Scalar
        long count = manager.newSelect(countRowIdAsCount())
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .selectScalar(Long.class);

        // You can map to your POJO or using Rowmapper
        String title = manager.newSelect(countRowIdAsCount())
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .selectOne(new RowMapper<String>() {
                    @Override
                    public String mapRow(Cursor cursor) {
                        return cursor.getString(cursor.getColumnIndex("TITLE"));
                    }
                });
```

### SELECT using TwoWaySQL
###### What is TwoWaySQL
'TwoWaySQL' is a concept, looks like a Template Engine for SQL.
This is based on Japanese O/R mapping framework [DBFlute](http://dbflute.seasar.org)
2WaySQL is the plain old SQL template. You can specify parameters and conditions using SQL comment.
So these SQLs are executable using SQL client tools.


``` asset/com/example/Notes/select.sql
   SELECT 
     TITLE,
	 BODY,
	 DATE
   FROM 
     NOTES 
    /*BEGIN*/
    WHERE
       /*IF pmb.date != null*/ 
	   DATE = /*pmb.date*/33333333333
	   /*END*/ 
    /*END*/ 
```

you select data by TwoWaySql using <code>manager.newSelectBySqlFile()</code>
if Notes.class's packageName is com.example  
framework references asset/com/example/Notes/select.sql and execute

```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    // as cursor
    Cursor cursor = manager.newSelectBySqlFile(Notes.class, "select.sql")
               .setParameter("date", new Date())
               .selectCursor();
               
```

### License
```java
/*******************************************************************************
 * Copyright (C) 2013-2015 Kem
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
```

