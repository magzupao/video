import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IVideoCredito } from '../video-credito.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../video-credito.test-samples';

import { VideoCreditoService } from './video-credito.service';

const requireRestSample: IVideoCredito = {
  ...sampleWithRequiredData,
};

describe('VideoCredito Service', () => {
  let service: VideoCreditoService;
  let httpMock: HttpTestingController;
  let expectedResult: IVideoCredito | IVideoCredito[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(VideoCreditoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a VideoCredito', () => {
      const videoCredito = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(videoCredito).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a VideoCredito', () => {
      const videoCredito = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(videoCredito).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a VideoCredito', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of VideoCredito', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a VideoCredito', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addVideoCreditoToCollectionIfMissing', () => {
      it('should add a VideoCredito to an empty array', () => {
        const videoCredito: IVideoCredito = sampleWithRequiredData;
        expectedResult = service.addVideoCreditoToCollectionIfMissing([], videoCredito);
        expect(expectedResult).toEqual([videoCredito]);
      });

      it('should not add a VideoCredito to an array that contains it', () => {
        const videoCredito: IVideoCredito = sampleWithRequiredData;
        const videoCreditoCollection: IVideoCredito[] = [
          {
            ...videoCredito,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addVideoCreditoToCollectionIfMissing(videoCreditoCollection, videoCredito);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a VideoCredito to an array that doesn't contain it", () => {
        const videoCredito: IVideoCredito = sampleWithRequiredData;
        const videoCreditoCollection: IVideoCredito[] = [sampleWithPartialData];
        expectedResult = service.addVideoCreditoToCollectionIfMissing(videoCreditoCollection, videoCredito);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(videoCredito);
      });

      it('should add only unique VideoCredito to an array', () => {
        const videoCreditoArray: IVideoCredito[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const videoCreditoCollection: IVideoCredito[] = [sampleWithRequiredData];
        expectedResult = service.addVideoCreditoToCollectionIfMissing(videoCreditoCollection, ...videoCreditoArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const videoCredito: IVideoCredito = sampleWithRequiredData;
        const videoCredito2: IVideoCredito = sampleWithPartialData;
        expectedResult = service.addVideoCreditoToCollectionIfMissing([], videoCredito, videoCredito2);
        expect(expectedResult).toEqual([videoCredito, videoCredito2]);
      });

      it('should accept null and undefined values', () => {
        const videoCredito: IVideoCredito = sampleWithRequiredData;
        expectedResult = service.addVideoCreditoToCollectionIfMissing([], null, videoCredito, undefined);
        expect(expectedResult).toEqual([videoCredito]);
      });

      it('should return initial array if no VideoCredito is added', () => {
        const videoCreditoCollection: IVideoCredito[] = [sampleWithRequiredData];
        expectedResult = service.addVideoCreditoToCollectionIfMissing(videoCreditoCollection, undefined, null);
        expect(expectedResult).toEqual(videoCreditoCollection);
      });
    });

    describe('compareVideoCredito', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareVideoCredito(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 6706 };
        const entity2 = null;

        const compareResult1 = service.compareVideoCredito(entity1, entity2);
        const compareResult2 = service.compareVideoCredito(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 6706 };
        const entity2 = { id: 9058 };

        const compareResult1 = service.compareVideoCredito(entity1, entity2);
        const compareResult2 = service.compareVideoCredito(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 6706 };
        const entity2 = { id: 6706 };

        const compareResult1 = service.compareVideoCredito(entity1, entity2);
        const compareResult2 = service.compareVideoCredito(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
