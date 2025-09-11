import { TestBed } from '@angular/core/testing';

import { MayaApiService } from './maya-api.service';

describe('MayaApiService', () => {
  let service: MayaApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MayaApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
