import { Component, ViewChild, ElementRef } from '@angular/core';
import { MatTable } from '@angular/material/table';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { FormControl, FormGroup } from '@angular/forms';

export interface Column {
  key: string;
  name: string;
  type: string;
  nullable: string;
  description: string;
}

export interface EntityCodeGenerateRequest {
  tableName: string;
  enversAudit: boolean;
  columns: Column[];
}

@Component({
  selector: 'app-entity-table',
  templateUrl: './entity-table.component.html',
  styleUrls: ['./entity-table.component.css'],
})
export class EntityTableComponent {
  constructor(private http: HttpClient) {}

  ngOnInit(): void {}

  @ViewChild(MatTable) table!: MatTable<Column>;

  tableMetaForm = new FormGroup({
    tableName: new FormControl(''),
    enversAudit: new FormControl(''),
  });

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
  tableName: string = '';
  text: string = '';
  displayedColumns = [
    'Key',
    'Name',
    'Type',
    'Nullable',
    'Description',
    'Delete',
  ];
  enversAudit: boolean = true;
  tableNameValid: boolean = true;
  varcharLengthMap: Map<number, string> = new Map();
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

    this.isGenerateButtonDisabled = true;
    setTimeout(() => {
      this.isGenerateButtonDisabled = false;
    }, 5000);

    const data: Column[] = this.columns.map((column, index) => {
      var type: string;
      if (column.type === 'VARCHAR') {
        type = `${column.type}(${this.varcharLengthMap.get(index)})`;
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

    const requstBody: EntityCodeGenerateRequest = {
      tableName: this.tableName,
      enversAudit: this.enversAudit,
      columns: data,
    };

    this.http
      .post(`${environment.apiUrl}/entity/table`, requstBody, {
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
}
