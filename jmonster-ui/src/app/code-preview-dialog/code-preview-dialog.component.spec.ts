import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodePreviewDialogComponent } from './code-preview-dialog.component';

describe('CodePreviewDialogComponent', () => {
  let component: CodePreviewDialogComponent;
  let fixture: ComponentFixture<CodePreviewDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CodePreviewDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CodePreviewDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
