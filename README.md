# 在庫管理アプリケーション

## 概要
このアプリケーションは、商品マスタ管理、メーカー管理、在庫入出庫管理、在庫履歴管理を一体で扱う業務向けシステムです。

主な目的は、在庫数の正確な管理と、業務上重要な整合性の担保です。具体的には、以下のような課題に対応しています。

- 商品ごとの在庫数を正確に保持する
- メーカー別の在庫を分離して管理する
- 出庫時に在庫不足やマイナス在庫を防ぐ
- 履歴と現在庫が一致していることを維持する
- 不正な入力や存在しないデータを安全に拒否する

複数の業務ルールを実装し、API・データ整合性・テストまで含めて検証しました。

---

## 画面イメージ

### 商品一覧
![商品一覧画面](images/product-list.png)

### 在庫推移グラフ
![在庫推移グラフ](images/stock-graph.png)

### 価格変更履歴
![価格変更履歴画面](images/price-history-up.png)

---

## 技術スタック

- Java 21
- Spring Boot 3.x
- Spring MVC
- Spring Data JPA
- Spring Security
- Vue 3
- Axios
- Apache ECharts
- MySQL
- H2 (テスト用)
- Thymeleaf
- Gradle
- Docker / Docker Compose
- JUnit 5
- MockMvc
- Git / GitHub

---

## 実装した機能

### 商品マスタ管理
- 商品の新規登録
- 商品の編集
- 商品の削除
- 商品名、カテゴリ、単位、在庫数、メーカー情報の管理
- メーカー必須化対応

### メーカー管理
- メーカーの登録・更新・削除
- 商品とメーカーの紐付け
- メーカー別在庫の管理基盤として設計

### 価格管理
- 商品の仕入価格を適用開始日を指定して変更
- 価格の開始日・終了日を履歴として管理
- 指定した商品ごとの価格変更履歴を表示

### 在庫管理
- 入庫処理
- 出庫処理
- 在庫数の自動更新
- 出庫時の在庫不足チェック
- 0以下や不正な数量の受け入れ拒否
- メーカー別の在庫内訳更新

### 履歴管理
- 入庫・出庫の履歴保存
- 履歴ごとに数量、種別、残在庫、メーカー、商品名を保存
- 時系列で履歴を取得可能

### 在庫サマリー
- 商品ごとの現在在庫を集計
- メーカー別の在庫状況を表示
- 履歴と現在庫の整合性を保つ

---

## 実務で意識した設計ポイント

### 1. 業務ルールをコードで明確化
単なる CRUD ではなく、以下のようなビジネスルールを実装しました。

- メーカーは必須
- 在庫が不足している場合は出庫不可
- 取引先別の在庫にも不足があれば失敗
- 0や負数の数量は受付不可
- 存在しない商品またはメーカーは拒否

### 2. 在庫の整合性を重視
在庫に関する処理では、商品テーブルの在庫とメーカー別内訳の両方を確認し、両方の整合性を保つ設計にしました。

### 3. 同時実行時の整合性を考慮
同時出庫時に在庫がマイナスになる問題を防ぐため、PESSIMISTIC_WRITE を利用したロック制御を導入しました。

### 4. 例外系に対するテストを重視
在庫不足、要求不正、存在しない ID、必須項目欠落などの異常系をテストで確認し、データが破損しないようにしました。

---

## アーキテクチャ

本アプリは、Spring Boot を中心とした 3 層構造で実装しています。

- Controller: HTTP リクエストの受付と画面制御
- Service: 業務ロジックとバリデーション
- Repository: JPA による永続化処理
- Entity: ドメインモデル
- Security: 認証・認可

```mermaid
graph LR
    UI[Thymeleaf / Web UI] --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> DB[(MySQL / H2)]
```

## 業務フロー図

```mermaid
flowchart TD
    A[商品・メーカーを登録] --> B{メーカーは必須?}
    B -- Yes --> C[商品マスタを保存]
    B -- No --> D[入力エラーで拒否]
    C --> E[入庫処理]
    E --> F[現在庫を更新]
    F --> G[在庫履歴を保存]
    G --> H{出庫要求}
    H -- Yes --> I[出庫数量を確認]
    I --> J{在庫不足?}
    J -- No --> K[出庫を実行]
    J -- Yes --> L[処理失敗]
    K --> M[在庫を減算]
    M --> N[履歴を記録]
    N --> O[在庫一覧を表示]
    H -- No --> O
    D --> P[ユーザーにエラー表示]
    L --> P

    classDef success fill:#dff7e8,stroke:#2e8b57,color:#1f2d1f;
    classDef danger fill:#f8d7da,stroke:#b02a37,color:#3b1f20;
    classDef process fill:#eaf2ff,stroke:#3b82f6,color:#1f2d1f;

    class C,E,F,G,K,M,N,O success;
    class D,J,L,P danger;
    class A,B,H,I process;
```

この業務フローでは、商品登録・入庫・出庫・履歴保存が一連の流れとして扱われており、在庫不足や不正入力を処理の途中で止める設計になっています。

## ER図

```mermaid
erDiagram
    MAKER ||--o{ PRODUCT : owns
    MAKER ||--o{ STOCK_DETAIL : manages
    MAKER ||--o{ STOCK_HISTORY : records
    PRODUCT ||--o{ STOCK_DETAIL : contains
    PRODUCT ||--o{ STOCK_HISTORY : logs
    PRODUCT ||--o{ PRODUCT_PRICE : has

    USER {
        int id PK
        varchar username UK
        varchar password
        varchar role
    }

    MAKER {
        int id PK
        varchar name
    }

    PRODUCT {
        int id PK
        varchar name
        int stock
        varchar unit
        varchar category
        int maker_id FK
        int cost_price
        int sale_price
        datetime created_at
        datetime updated_at
    }

    STOCK_DETAIL {
        int id PK
        int product_id FK
        int maker_id FK
        int quantity
    }

    STOCK_HISTORY {
        int id PK
        int product_id FK
        varchar product_name
        int quantity
        int maker_id FK
        varchar unit
        varchar category
        datetime date_time
        varchar type
        int stock
    }

    PRODUCT_PRICE {
        int id PK
        int product_id FK
        int cost_price
        date start_date
        date end_date
        datetime created_at
    }
```

メーカーと商品は 1:N の関係で、商品ごとの在庫は `stock_detail` と `stock_history` に記録されます。これにより、現在庫と履歴の整合性を保ちつつ、メーカー別の在庫管理を実現しています。

---

## セキュリティ対応

- Spring Security による認証・認可を導入
- パスワードは BCrypt でハッシュ化して保存
- ロールベースのアクセス制御を実装
- 管理者と一般ユーザーで権限を分離

実運用を想定して以下を追加で検討する予定です。

- HTTPS 強制
- セッション/クッキーの安全設定
- ログイン失敗時の制限
- API 認証の見直し

---

## テスト戦略

### 単体テスト
- 在庫不足時の例外検証
- 商品未検出時の例外検証
- メーカー別在庫不足の検証

### 結合テスト
以下のような業務シナリオを実際のアプリケーションとして検証しました。

- 商品登録 → 入庫 → 出庫 → 履歴 → 集計の正常系
- 複数メーカーでの在庫分離
- 0、負数、在庫不足、必須項目欠落の異常系
- 存在しない商品・メーカーへの対応
- 商品削除時の整合性確認
- 同時出庫時の安全性確認

実際に以下の結合テストを含めています。

- `StockFlowIntegrationTest`
- `StockOutServiceTest`

---

## 実装上の工夫

### 1. 業務ルールをテストで担保
単に動くだけではなく、次のようなルールをテストで確認しました。

- 在庫が負になることを防ぐ
- 必須項目が欠けると処理に失敗する
- 履歴と現在庫が整合している
- リクエストが不正な場合にデータが壊れない

### 2. 変更に強い設計
業務ルールが複雑になっても、Service 層でロジックを分離しているため、更新や修正が比較的しやすい構成です。

### 3. 実装時のルール
このプロジェクトでは、単なる機能実装ではなく、以下のような実務に近い視点を意識しました。

- 重要なビジネスルールの明確化
- データ整合性の維持
- 例外処理とテスト設計
- 安全な在庫操作の実装

---

## ローカル開発環境

### 前提条件
- Java 21
- Docker / Docker Compose
- Gradle

現在はローカル開発・検証用途として MySQL のみをコンテナ起動しており、アプリケーション本体は Spring Boot で直接実行して検証しています。
本番環境へのデプロイおよび本番運用構成までは未対応です。

---

## プロジェクト構成

```text
src/
  main/
    java/
      com/example1/demo3/
        config/
        controller/
        dto/
        entity/
        exception/
        repository/
        service/
  test/
    java/
      com/example1/demo3/
        integration/
        unit/
```

---

## 今後の改善予定

- 期間別の在庫推移分析機能
- 価格履歴の編集/削除対応
- ユーザー情報更新機能の追加
- Controller 層の追加テスト拡充
- 本番向けセキュリティ強化

---

## 実装で工夫した点

- 商品全体の在庫とメーカー別の在庫内訳を一致させながら、入出庫履歴も正しく記録することに苦労しました。特に出庫処理では、商品全体と対象メーカーの在庫をそれぞれ確認し、同時に出庫要求があった場合にも在庫が不足しないよう、ロック制御を取り入れました。また、在庫の更新と履歴の保存をトランザクション内で行い、処理に失敗した際にデータの一部だけが更新されないようにしています。これらの仕組みを実装する過程で、ロック制御とトランザクション管理への理解を深め、結合テストで在庫不足や同時出庫時の動作を確認しました。
- 商品削除時の関連データの扱いにも注意しました。在庫内訳は削除しつつ、過去の入出庫履歴は後から確認できるように、商品名を履歴に保持して商品との関連を解除する設計にしています。これにより、マスタデータの削除後も取引履歴を参照できます。
- 在庫推移を視覚的に確認できるよう、商品ごとの履歴をAPIから取得し、日時と在庫数をグラフ用データに整形して表示しました。入庫・出庫の数量推移も個別に可視化し、在庫推移グラフでは折れ線と棒グラフを切り替えられるようにしています。商品選択後のデータ再取得やグラフの再描画にも対応しました。
- 入力ミスや誤操作を減らすため、商品フォームではマスタ情報を選択式で表示し、メーカー未選択時には保存できないようにしました。価格変更画面では価格や適用開始日の入力を確認し、在庫一覧ではメーカー別の内訳を確認できるようにしています。また、削除前に確認を挟むことで、意図しない操作を防ぐようにしました。

