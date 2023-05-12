import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { CodeModel } from '@ngstack/code-editor';
import { MatDialog } from '@angular/material/dialog';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { WarnDialogComponent } from '../warn-dialog/warn-dialog.component';
import { CodePreviewDialogComponent } from '../code-preview-dialog/code-preview-dialog.component';
import { Column } from '../interface/Column';
import { EntityCodeGenerateByPlainTextRequest } from '../interface/EntityCodeGenerateByPlainTextRequest';
import { DialogIconType } from '../enum/DialogIconType';

@Component({
  selector: 'app-plain-text',
  templateUrl: './plain-text.component.html',
  styleUrls: ['./plain-text.component.css']
})
export class PlainTextComponent implements OnInit {

  constructor(private http: HttpClient, public dialog: MatDialog) { }

  ngOnInit(): void {
    this.codeEditorModel.value = this.code;
  }

  @Input() tableName: string = '';
  @Input() enversAudit: boolean = true;
  @Input() tableMetaForm = new FormGroup({
    tableName: new FormControl(''),
    enversAudit: new FormControl(''),
  });
  @Output() tableNameEvent = new EventEmitter<string>();

  code: string = "PK\tkeycloak_user_id\tUUID\tnotnull\tKeycloak使用者ID\nPK\terror_date\tDate\tnotnull\t錯誤日期\n\terror_count\tint\tnotnull\t錯誤次數";
  codeEditorTheme = 'vs-dark';
  codeEditorModel: CodeModel = {
    language: 'txt',
    uri: 'plainText',
    value: '',
  };

  options = {
    linenumbers: true,
    contextmenu: true,
    minimap: {
      enabled: false,
    },
  };

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

  onCodeChanged(value: any) {
    this.code = value;
  }

  preview() {

    if(this.code.trim() === '') {
      const diagLogRef = this.dialog.open(WarnDialogComponent, {
        data: { message: '請輸入內容', showInput: false, icon: DialogIconType.WARN },
      })
      return;
    }

    if (!this.tableMetaForm.controls.tableName.valid) {
      const diagLogRef = this.dialog.open(WarnDialogComponent, {
        data: { message: '請輸入表名稱', showInput: true, inputLabel: '表名稱', inputPlaceHolder: '請使用CamelCase', required: true },
        disableClose: true,
      })
      diagLogRef.afterClosed().subscribe((result: string) => {
        this.tableName = result;
        this.emitTableName(this.tableName)
        this.requestCode();
      });
      return;
    }

    this.requestCode();

  }

  private emitTableName(name: string) {
    this.tableNameEvent.emit(name)
  }

  private requestCode() {
    this.http
      .post(`${environment.apiUrl}/entity/text`, this.generateRequestBody(), {
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


  private generateRequestBody(): EntityCodeGenerateByPlainTextRequest {

    const requestBody: EntityCodeGenerateByPlainTextRequest = {
      tableName: this.tableName,
      enversAudit: this.enversAudit,
      text: this.code,
    };

    return requestBody;
  }

}
