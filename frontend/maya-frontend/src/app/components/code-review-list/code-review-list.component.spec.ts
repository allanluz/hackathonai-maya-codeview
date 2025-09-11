import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CodeReviewListComponent } from './code-review-list.component';

describe('CodeReviewListComponent', () => {
  let component: CodeReviewListComponent;
  let fixture: ComponentFixture<CodeReviewListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CodeReviewListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CodeReviewListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
