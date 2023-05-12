import { Component, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import { MatTable } from '@angular/material/table';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { CodePreviewDialogComponent } from '../code-preview-dialog/code-preview-dialog.component';
import { WarnDialogComponent } from '../warn-dialog/warn-dialog.component';
import { Column } from '../interface/Column';
import { EntityCodeGenerateByTableRequest } from '../interface/EntityCodeGenerateByTableRequest';
import { DialogIconType } from '../enum/DialogIconType';


@Component({
  selector: 'app-entity-table',
  templateUrl: './entity-table.component.html',
  styleUrls: ['./entity-table.component.css'],
})
export class EntityTableComponent {
  constructor(private http: HttpClient, public dialog: MatDialog) { }

  ngOnInit(): void { }

  @ViewChild(MatTable) table!: MatTable<Column>;

  @Input() tableName: string = '';
  @Input() enversAudit: boolean = true;
  @Input() tableMetaForm = new FormGroup({
    tableName: new FormControl(''),
    enversAudit: new FormControl(''),
  });
  @Output() tableNameEvent = new EventEmitter<string>();

  columns: Column[] = [
    {
      key: 'N',
      name: 'created_date',
      type: 'TIMESTAMP',
      nullable: 'NOTNULL',
      description: '新增日期',
    },
    {
      key: 'N',
      name: 'created_user_id',
      type: 'UUID',
      nullable: 'NOTNULL',
      description: '新增使用者ID',
    },
    {
      key: 'N',
      name: 'updated_date',
      type: 'TIMESTAMP',
      nullable: 'NOTNULL',
      description: '更新日期',
    },
    {
      key: 'N',
      name: 'updated_user_id',
      type: 'UUID',
      nullable: 'NOTNULL',
      description: '更新使用者 ID',
    },
  ];

  text: string = '';
  displayedColumns = [
    'Key',
    'Name',
    'Type',
    'Nullable',
    'Description',
    'Delete',
  ];

  tableNameValid: boolean = true;
  varcharLengthMap: Map<number, string> = new Map();
  numericPrecisionMap: Map<number, string> = new Map();
  numericScaleMap: Map<number, string> = new Map();
  isGenerateButtonDisabled: boolean = false;

  addEmptyColumn() {
    this.columns.push({
      key: 'N',
      name: '',
      type: 'UUID',
      nullable: 'NOTNULL',
      description: '',
    });
    this.table.renderRows();
  }

  addCreatedDateColumn() {
    this.columns.push({
      key: 'N',
      name: 'created_date',
      type: 'TIMESTAMP',
      nullable: 'NOTNULL',
      description: '新增日期',
    });
    this.table.renderRows();
  }

  addCreatedUserIdColumn() {
    this.columns.push({
      key: 'N',
      name: 'created_user_id',
      type: 'UUID',
      nullable: 'NOTNULL',
      description: '新增使用者ID',
    });
    this.table.renderRows();
  }

  addUpdatedDateColumn() {
    this.columns.push({
      key: 'N',
      name: 'updated_date',
      type: 'TIMESTAMP',
      nullable: 'NOTNULL',
      description: '新增日期',
    });
    this.table.renderRows();
  }

  addUpdatedUserIdColumn() {
    this.columns.push({
      key: 'N',
      name: 'updated_user_id',
      type: 'UUID',
      nullable: 'NOTNULL',
      description: '新增使用者ID',
    });
    this.table.renderRows();
  }

  onVarcharLengthChanged(target: EventTarget | null, index: number) {
    if (target == null) return;
    this.varcharLengthMap.set(index, (target as HTMLInputElement).value);
  }

  onNumericPrecisionChanged(target: EventTarget | null, index: number) {
    if (target == null) return;
    this.numericPrecisionMap.set(index, (target as HTMLInputElement).value);
  }

  onNumericScaleChanged(target: EventTarget | null, index: number) {
    if (target == null) return;
    this.numericScaleMap.set(index, (target as HTMLInputElement).value);
  }

  removeColumn() {
    this.columns.pop();
    this.table.renderRows();
  }

  deleteColumn(index: number) {
    this.columns.splice(index, 1);
    this.table.renderRows();
  }

  generateByTable() {
    if (!this.tableMetaForm.controls.tableName.valid) return;

    this.closeButtonTemporarily()

    this.http
      .post(`${environment.apiUrl}/entity/table`, this.generateRequestBody(), {
        responseType: 'arraybuffer',
      })
      .subscribe((response: any) => {
        // TODO 處理錯誤訊息
        // TODO 設定環境變數
        const blob = new Blob([response], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.tableName}.kt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      });
  }


  private closeButtonTemporarily() {

    this.isGenerateButtonDisabled = true;
    setTimeout(() => {
      this.isGenerateButtonDisabled = false;
    }, 5000);
  }

  private checkTableName(): boolean {
    if (this.tableName === null || this.tableName.trim().length === 0) {
      this.tableNameValid = false;
      return false;
    } else {
      this.tableNameValid = true;
      return true;
    }
  }

  generateByText() {
    // TODO 上傳檔案

    this.http
      .post(
        `${environment.apiUrl}/entity/text?tableName=${this.tableName}`,
        this.text,
        { responseType: 'arraybuffer' }
      )
      .subscribe((response: any) => {
        // TODO 處理錯誤訊息
        const blob = new Blob([response], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${this.tableName}.kt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      });
  }

  preview() {

    if (this.columns.length === 0) {
      this.dialog.open(WarnDialogComponent, {
        data: { message: '至少輸入一個欄位', showInput: false, icon: DialogIconType.WARN },
        disableClose: false,
      })
      return
    }

    if (!this.tableMetaForm.controls.tableName.valid) {
      const diagLogRef = this.dialog.open(WarnDialogComponent, {
        data: { message: '請輸入表名稱', showInput: true, inputLabel: '表名稱', inputPlaceHolder: '請使用CamelCase', required: true, icon: DialogIconType.INFO },
        disableClose: true,
      })
      diagLogRef.afterClosed().subscribe((result: string) => {
        this.tableName = result;
        this.emitTableName(this.tableName)
        this.requestEntityTableCode();
      });
      return;
    }

    this.requestEntityTableCode();

  }

  private emitTableName(name: string) {
    this.tableNameEvent.emit(name)
  }

  private requestEntityTableCode() {
    this.http
      .post(`${environment.apiUrl}/entity/table`, this.generateRequestBody(), {
        responseType: 'json',
      })
      .subscribe((response: any) => {
        this.dialog.open(CodePreviewDialogComponent, {
          data: { dataList: response, tableName: this.tableName },
          height: '70%',
          width: '60%',
        });
      });
  }

  private generateRequestBody(): EntityCodeGenerateByTableRequest {

    const data: Column[] = this.columns.map((column, index) => {
      var type: string;
      if (column.type === 'VARCHAR') {
        type = `${column.type}(${this.varcharLengthMap.get(index)})`;
      } else if (column.type === 'NUMERIC' || column.type === 'DECIMAL') {
        type = `${column.type}(${this.numericPrecisionMap.get(index)},${this.numericScaleMap.get(index)})`;

      } else {
        type = column.type;
      }

      return {
        key: column.key,
        name: column.name,
        type: type,
        nullable: column.nullable,
        description: column.description,
      };
    });

    const requestBody: EntityCodeGenerateByTableRequest = {
      tableName: this.tableName,
      enversAudit: this.enversAudit,
      columns: data,
    };

    return requestBody;
  }
}
