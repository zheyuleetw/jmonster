import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CodeModel } from '@ngstack/code-editor';


@Component({
  selector: 'app-code-preview-dialog',
  templateUrl: './code-preview-dialog.component.html',
  styleUrls: ['./code-preview-dialog.component.css']
})
export class CodePreviewDialogComponent implements OnInit {

  dataList: string[] = [];
  code: string = "";
  tableName: string = "";
  codeEditorTheme = 'vs';
  codeEditorModel: CodeModel = {
    language: 'kotlin',
    uri: 'main.kt',
    value: '',
  };

  options = {
    linenumbers: true,
    contextmenu: true,
    minimap: {
      enabled: false,
    },
  };

  constructor(@Inject(MAT_DIALOG_DATA) public data: { dataList: string[], tableName: string }) { }

  ngOnInit(): void {
    this.dataList = this.data.dataList;
    this.code = this.dataList.join("\n");
    this.tableName = this.data.tableName
    this.codeEditorModel.value = this.code;
  }

  download() {
    const blob = new Blob([this.code], { type: 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${this.tableName}.kt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  onCodeChanged(value: any) {
    this.code = value;
  }

}
