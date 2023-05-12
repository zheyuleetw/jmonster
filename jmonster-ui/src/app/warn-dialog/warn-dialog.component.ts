import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormControl, FormGroup } from '@angular/forms';
import { EntityTableComponent } from '../entity-table/entity-table.component';
import { DialogIconType } from '../enum/DialogIconType';

@Component({
  selector: 'app-warn-dialog',
  templateUrl: './warn-dialog.component.html',
  styleUrls: ['./warn-dialog.component.css']
})
export class WarnDialogComponent implements OnInit {

  constructor(@Inject(MAT_DIALOG_DATA) public data:
    {
      message: string,
      showInput: boolean,
      inputLabel: string,
      inputPlaceHolder: string,
      required: boolean,
      icon: DialogIconType,
    },
    private dialogRef: MatDialogRef<EntityTableComponent>) { }

  message: string = ""
  showInput: boolean = false;
  inputData: string = "";
  inputLabel: string = "";
  inputPlaceHolder: string = "";
  inputRequired: boolean = false;
  disableOK: boolean = true;
  form = new FormGroup({
    inputValue: new FormControl(''),
  });
  icon: DialogIconType = DialogIconType.INFO

  ngOnInit(): void {
    this.message = this.data.message;
    this.showInput = this.data.showInput;
    this.inputLabel = this.data.inputLabel;
    this.inputPlaceHolder = this.data.inputPlaceHolder;
    this.inputRequired = this.data.required;
    if (!this.showInput) {
      this.disableOK = false;
    }
    this.icon = this.data.icon;
  }

  send() {

    if (this.inputRequired) {
      if (this.form.controls.inputValue.invalid) {
        this.form.controls.inputValue.markAsTouched();
        return;
      } else {
        this.dialogRef.close(this.inputData);
      }
    } else {
      this.dialogRef.close(this.inputData);
    }
  }

  valid() {
    this.disableOK = this.form.controls.inputValue.invalid;
  }

}
