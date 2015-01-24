# MonoTalk
このライブラリはAndroid用のORマッピングライブラリです。
煩雑な実装になりがちなAndroidのDB周辺実装を簡略化することを目的としています。  
アンドロイドアプリを作っていたつもりでしたが、アプリはできずにライブラリが。。      
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
- 自然キーとなる項目には、Columnアノテーションのuniqe属性、  
もしくは、@UniqeアノテーションでUniqe制約を付与してください。  

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
    notes.setId(id);
    notes.title = "Title1";
    notes.body = "Hello World1";
    manager.update(notes);

	// ## Null除外する際は、updateExcludesNullが使用可能です。
	Notes notes = new Notes();
    long id = 111l;
    notes.setId(id);
    notes.title = null;
    notes.body = "Hello World1";
	manager.updateExcludesNull(notes);
	
	// ## SQliteDatabaseと同じようなIFでの更新が可能です
    ContentValues value = new ContentValues();
    value.put(Notes.TITLE, "TEST");
    manager.updateById(Notes.class, value, 1l);
```
DELETE
```java
	EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    long id = 111l;
    notes.setId(id);
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

```java
    EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    manager.newInsertInto(Notes.class).value(Notes.TITLE, "TEST").execute();
	
	// monotalk.db.query.QueryBuilder#toValuesExcludesColumns()をstatic importして、
	// EntityオブジェクトからContentsValueを生成して登録します。
	EntityManager manager = MonoTalk.getDBManagerByDefaultDbName();
    Notes notes = new Notes();
    notes.body = "Hello World1";
    manager.newInsertInto(Notes.class).values(toValuesExcludesColumns(notes, Notes.TITLE)).execute();
	
	
```
update,deleteについては今後ドキュメントを作成します。
