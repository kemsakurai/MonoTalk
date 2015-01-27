# MonoTalk
このライブラリはAndroid用のORマッピングライブラリです。
煩雑な実装になりがちなAndroidのDB周辺実装を簡略化することを目的としています。  
アンドロイドアプリを作っていたつもりでしたが、アプリはできずにライブラリとなりました。

[Active Android](https://github.com/pardom/ActiveAndroid), [Ollie](https://github.com/pardom/ollie/), [Sprinkles](https://github.com/emilsjolander/sprinkles),
[DBFlow](https://github.com/Raizlabs/DBFlow),[greenDAO](http://greendao-orm.com),[DBFlute](http://dbflute.seasar.org),[S2JDBC](http://s2container.seasar.org/2.4/ja/s2jdbc.html)
等の実装を参考にしながら作成しています。  
基本機能は動作しますが、外部IFなどはまだ変更予定です。


## 機能概要
当ライブラリには以下の機能があります。
- Entityクラスに記述したアノテーションからテーブルを作成します。
- 複数DBの管理が可能です。
- TABLEクラスからクエリを作成し、単純なCRUDな可能です。
- 少し難しいクエリはクエリオブジェクトを使用して作成が可能です。
- 難しいクエリは2waysqlを作成して実行が可能です。


## 導入方法
build.gradleに以下のmavenリポジトリの設定を追加してください。  

    repositories {
          maven { url 'https://kemsakurai.github.io/maven/master/releases' }
    }
    
build.gradleの以下の依存関係を追加してください。  
    
    dependencies {
         compile 'MonoTalk:monotalk-db:0.0.1'
    }

## 使用方法

Applicationを継承したクラスに以下のような記述を追加してください。  

```java
	public class SampleApplication extends android.app.Application {
	    
		@Override
    	public void onCreate() {
        	super.onCreate();

			// builderをnewする
	        DatabaseConfigration.Builder builder = new DatabaseConfigration.Builder();
    	    // batabase名を設定
			builder.setDataBaseName("NotesDB");
			// version番号を設定
        	builder.setVersion(1);
			// default(プライマリ?) Databaseとして使用する場合,true (初期値true)
        	builder.setDefalutDatabase(true);
        	// Databaseに関連づけするEntityクラスを設定
			builder.addTable(Notes.class);
        	builder.addTable(NoteTag.class);
        	builder.addTable(Tag.class);
			// 初期化
        	MonoTalk.init(getApplicationContext(), builder.create());
		}
		
		@Override
	    public void onTerminate() {
        	super.onTerminate();
	        MonoTalk.dispose();
    	}
	}
```

## Entity(Table)クラスの作成

- DATABASEのテーブル定義に対応するEntityクラスを作成します。  
(初期化時にEntityクラスの指定が必要になるので、実際はこちらを先に実施すると思います。)  
- EntityクラスはEntityを継承して作成する必要があります。  
- @Tableアノテーションで作成するTableを指定し、    
@Columnアノテーションで作成するColumn名を指定します。  
- @ColumnにはPK指定はできません。  
(Create Table DDL実行時に、_id カラムをAUTOINCREMENT 指定の主キーとして作成します。)  
- 自然キーとなる項目には、Columnアノテーションのunique属性、  
もしくは、@Uniqueアノテーションでunique制約を付与してください。  

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
## データの登録 更新 削除  

  EntityManagerクラスを使用した単純なCRUD    
  クエリオブジェクトを使用したCRUD  
	2waysqlを使用したCRUDが実行できます。  
	*2waysqlは現在READのみ可能です。
	
#### EntityManagerクラスを使用したCRUD
INSERT  
```java
	EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    notes.title = "Title1";
    notes.body = "Hello World1";
    long id = manager.insert(notes);

	// ## Null除外する際は、insertExcludesNullが使用可能です。
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

	// ## Null除外する際は、updateExcludesNullが使用可能です。
	Notes notes = new Notes();
    long id = 111l;
    notes.id = id;
    notes.title = null;
    notes.body = "Hello World1";
    manager.updateExcludesNull(notes);
	
    // ## SQliteDatabaseと同じIFでの更新
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

    // Class と IDを指定して削除する
    manager.deleteById(Notes.class, 111l);
    // Class Where句 Bind変数を指定して削除する
    manager.delete(Notes.class, "_id=?", 111l);
    // Table Where句 Bind変数を指定して削除する
    manager.delete("NOTES",  "_id=?", 111l);	
```

### クエリオブジェクトを使用したCRUD  
クエリオブジェクトはentitymanagrのnew...で始まるメソッドを使用して生成します。  
INSERT
```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    manager.newInsertInto(Notes.class).value(Notes.TITLE, "TEST").execute();
	
    // monotalk.db.query.QueryUtils#from()をstatic importして、 
    // EntityオブジェクトからContentsValueを生成して登録します。
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    notes.body = "Hello World1";
    manager.newInsert(Notes.class).values(from(notes, Notes.TITLE)).execute();
	
```
UPDATE
```java
        EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
        manager.newUpdate(Notes.class).as("Notes")
                .value("TITLE", "Hello")
                .where("_id").eq(111l);
        
		
        Notes notes = new Notes();
        notes.id = 100l;
        // monotalk.db.query.QueryUtils#idEquals()をstatic importして、 
        // EntityオブジェクトからContentsValueを生成して登録します。
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
        
        // Cursorで取得
        Cursor cursor = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .limit(2)
                .offset(3).selectCursor();

        // Entity 1要素を取得
        Notes notes = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectOne();

        // EntityListで取得
        List<Notes> noteList = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectList();

        // LazyListで取得
        LazyList<Notes> noteList = manager.newSelect(
                "TITLE",
                "BODY",
                "DATE")
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .orderBy("TITLE")
                .selectLazyList();

        // スカラー値を取得
        long count = manager.newSelect(countRowIdAsCount())
                .from(Notes.class)
                .where("DATE").eq(new Date())
                .selectScalar(Long.class);

        // Entityではないものにマッピングして取得
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

### TWOWAYSQLを使用したSELECT  
``` select.sql
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
上記のクエリを以下の記述で実行可能です。
仮に Notes.classのパッケージがcom.exampleであった場合は  
assetディレクトリ配下の asset/com/example/Notes/select.sql を参照し、実行します。

```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    // cursorで取得
    Cursor cursor = manager.newSelectBySqlFile(Notes.class, "select.sql")
               .setParameter("date", new Date())
               .selectCursor();
```
*TWOWAYSQL parser実装は、[DBFlute](http://dbflute.seasar.org)のTWOWAYSQL parserを拝借しています。
