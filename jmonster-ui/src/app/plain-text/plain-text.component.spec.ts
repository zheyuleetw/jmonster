import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlainTextComponent } from './plain-text.component';

describe('PlainTextComponent', () => {
  let component: PlainTextComponent;
  let fixture: ComponentFixture<PlainTextComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PlainTextComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PlainTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
