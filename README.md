# MonoTalk
このライブラリはAndroid用のORマッピングライブラリです。
煩雑な実装になりがちなAndroidのDB周辺実装を簡略化することを目的としています。
アンドロイドアプリを作っていたつもりでしたが、アプリはできずにライブラリが。。    
[Active Android](https://github.com/pardom/ActiveAndroid), [Ollie](https://github.com/pardom/ollie/), [Sprinkles](https://github.com/emilsjolander/sprinkles),
[DBFlow](https://github.com/Raizlabs/DBFlow),[greenDAO](http://greendao-orm.com),[DBFlute](http://dbflute.seasar.org),[S2JDBC](http://s2container.seasar.org/2.4/ja/s2jdbc.html)
等の実装を参考にしながら作成しています。
基本機能は動作しますが、外部IFなどはまだ変更するつもりです。


## Feature
当ライブラリには以下の機能があります。
- Entityクラスに記述したアノテーションからテーブルを作成します。
- 複数DBの管理が可能です。
- TABLEクラスからクエリを作成し、単純なCRUDな可能です。
- 少し難しいクエリはクエリオブジェクトを使用して作成が可能です。
- 難しいクエリは2waysqlを作成して実行が可能です。

## 使用方法
そのうち書きます。。

