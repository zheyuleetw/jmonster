import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EntityCodeGeneratorComponent } from './entity-code-generator.component';

describe('EntityCodeGeneratorComponent', () => {
  let component: EntityCodeGeneratorComponent;
  let fixture: ComponentFixture<EntityCodeGeneratorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EntityCodeGeneratorComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EntityCodeGeneratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
