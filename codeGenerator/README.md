# Code Generator 程式碼產生器

## Entity Code Generator

1. 在 Project 目錄路徑下，執行以下指令將 EntityCodeGenerator 編譯並產生 jar 檔輸出至 /entity-code-generator/build/libs

   ```cmd

    ./gradlew :entity-code-generator:build

   ```

2. 建立 Entity 的 txt 檔案，檔案名稱將會做為產生的類別名稱

   ![image](https://user-images.githubusercontent.com/70104159/200456018-2708e081-463c-4baa-9d2e-575bc6778284.png)

   - 可直接複製 Google Sheet 上的內容，貼上到空的 txt 檔案內

   ![image](https://user-images.githubusercontent.com/70104159/200456920-67a9efbf-90da-44e6-ae0d-bfa276f18747.png)

   ![image](https://user-images.githubusercontent.com/70104159/200456344-95e3cc83-4550-46d6-a3df-6311d95eb5fd.png)

3. 可用參數

   |  參數名 | 多參或單參  | 必填 | 說明  | 錯誤訊息  |
   |  ----  | ----  | ----  | ----  | ----  |
   | --source  | 多 | Y |欲產生 Entity 的規格 txt 檔案 的**絕對路徑** | Please declare --source|
   | --destination  | 單 | N | 產生 Entity 的檔案存放位置，若沒有填，將會在 txt 檔案路徑新增一個同名的 folder，產生的程式碼將會存放在這個 folder||

4. 支援項目

   1. 類型對應
      |  Postgres | Kotlin  |
      |  ----  | ----  |
      |  UUID | UUID  |
      |  JSONB | I18nNameMap  |
      |  BOOL | Boolean  |
      |  TIMESTAMP | LocalDateTime  |
      |  DATETIME | LocalDateTime  |
      |  TEXT | String  |
      |  TIME | LocalTime  |
      |  INT | Int  |
      |  *default | String  |
   2. 支援 key
      |  Key | 使否支援複合鍵  | 說明 |
      |  ----  | ----  | ----  |
      |  PK | Y  |若為複合主鍵，產生對應的 PrimaryKey Class|
      |  UK | Y  |若為複合鍵，在 `@Table` 加上 `uniqueConstraints = [UniqueConstraint(columnNames = [field1, field2])])`|
   3. 支援註解
      | Target |  Annotation | 說明 |
      |----|  ----  | ----  |
      |  CLASS  |  `@Audited` | |
      |  CLASS  |  `@Entity` |  |
      |  CLASS  |  `@Table` |   |
      |  CLASS  |  `@EntityListeners` | 有更新時間、更新使用者、創建時間、創建使用者時會加上此註解 |
      |  CLASS  |  `@IdClass` | 有複合主鍵時會加上此註解|
      |  CLASS  |  `@TypeDefs` | 有 `jsonb` 型別時會加上此註解  |
      |  FIELD  |  `@Column` | 1. non null 時加上 `nullable = false` 2.創建時間、創建使用者加上 `updatable = false` 3. 單一 UK 加上 `unique = true` 4. jsonb 欄位加上 `columnDefinition = "jsonb"` |
      |  FIELD  |  `@LastModifiedDate` |更新時間會加上此註解 |
      |  FIELD  |  `@LastModifiedBy` |更新使用者會加上此註解 |
      |  FIELD  |  `@CreatedDate` |創建時間加上此註解 |
      |  FIELD  |  `@CreatedBy` |創建使用者會加上此註解 |

5. 使用範例: 在 /entity-code-generator/build/libs 路徑下執行以下指令

   1. 通用格式
      1. {version}: 版號
      2. {file}: txt 檔案的絕對路徑

      ```cmd

            java -jar entity-code-generator-{version}.jar --source="{file}"

      ```

   2. 單一檔案，未指定存放位置

      ```cmd
      
      java -jar entity-code-generator-1.5.jar --source="C:\Users\ken10\OneDrive\文件\notes\entity\EquipmentsColor.txt" 

      ```

   3. 單一檔案，指定存放位置

      ```cmd

      java -jar entity-code-generator-1.5.jar --source="C:\Users\ken10\OneDrive\文件\notes\entity\EquipmentsColor.txt" --destination="C:\workspace\ec-master-module\core\src\main\kotlin\com\umh\ecmaster\core\model\ec\master"

      ```

   4. 多檔案，未指定存放位置

      ```cmd

      java -jar entity-code-generator-1.5.jar --source="C:\Users\ken10\OneDrive\文件\notes\entity\EquipmentsColor.txt" --source="C:\Users\ken10\OneDrive\文件\notes\entity\Equipments.txt"

      ```

   5. 多檔案，指定存放位置

      ```cmd

      java -jar entity-code-generator-1.5.jar --source="C:\Users\ken10\OneDrive\文件\notes\entity\EquipmentsColor.txt" --source="C:\Users\ken10\OneDrive\文件\notes\entity\Equipments.txt" --destination="C:\workspace\ec-master-module\core\src\main\kotlin\com\umh\ecmaster\core\model\ec\master"

      ```
