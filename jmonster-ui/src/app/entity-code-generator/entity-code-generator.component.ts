import { Component, OnInit, Input } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-entity-code-generator',
  templateUrl: './entity-code-generator.component.html',
  styleUrls: ['./entity-code-generator.component.css']
})
export class EntityCodeGeneratorComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  @Input() tableName: string = '';
  @Input() enversAudit: boolean = true;
  @Input() tableMetaForm = new FormGroup({
    tableName: new FormControl(''),
    enversAudit: new FormControl(''),
  });

  setTableName(name: string) {
    this.tableName = name
  }

}
