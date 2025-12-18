import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IVideoImagen } from '../video-imagen.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../video-imagen.test-samples';

import { VideoImagenService } from './video-imagen.service';

const requireRestSample: IVideoImagen = {
  ...sampleWithRequiredData,
};

describe('VideoImagen Service', () => {
  let service: VideoImagenService;
  let httpMock: HttpTestingController;
  let expectedResult: IVideoImagen | IVideoImagen[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(VideoImagenService);
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

    it('should create a VideoImagen', () => {
      const videoImagen = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(videoImagen).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a VideoImagen', () => {
      const videoImagen = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(videoImagen).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a VideoImagen', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of VideoImagen', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a VideoImagen', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addVideoImagenToCollectionIfMissing', () => {
      it('should add a VideoImagen to an empty array', () => {
        const videoImagen: IVideoImagen = sampleWithRequiredData;
        expectedResult = service.addVideoImagenToCollectionIfMissing([], videoImagen);
        expect(expectedResult).toEqual([videoImagen]);
      });

      it('should not add a VideoImagen to an array that contains it', () => {
        const videoImagen: IVideoImagen = sampleWithRequiredData;
        const videoImagenCollection: IVideoImagen[] = [
          {
            ...videoImagen,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addVideoImagenToCollectionIfMissing(videoImagenCollection, videoImagen);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a VideoImagen to an array that doesn't contain it", () => {
        const videoImagen: IVideoImagen = sampleWithRequiredData;
        const videoImagenCollection: IVideoImagen[] = [sampleWithPartialData];
        expectedResult = service.addVideoImagenToCollectionIfMissing(videoImagenCollection, videoImagen);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(videoImagen);
      });

      it('should add only unique VideoImagen to an array', () => {
        const videoImagenArray: IVideoImagen[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const videoImagenCollection: IVideoImagen[] = [sampleWithRequiredData];
        expectedResult = service.addVideoImagenToCollectionIfMissing(videoImagenCollection, ...videoImagenArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const videoImagen: IVideoImagen = sampleWithRequiredData;
        const videoImagen2: IVideoImagen = sampleWithPartialData;
        expectedResult = service.addVideoImagenToCollectionIfMissing([], videoImagen, videoImagen2);
        expect(expectedResult).toEqual([videoImagen, videoImagen2]);
      });

      it('should accept null and undefined values', () => {
        const videoImagen: IVideoImagen = sampleWithRequiredData;
        expectedResult = service.addVideoImagenToCollectionIfMissing([], null, videoImagen, undefined);
        expect(expectedResult).toEqual([videoImagen]);
      });

      it('should return initial array if no VideoImagen is added', () => {
        const videoImagenCollection: IVideoImagen[] = [sampleWithRequiredData];
        expectedResult = service.addVideoImagenToCollectionIfMissing(videoImagenCollection, undefined, null);
        expect(expectedResult).toEqual(videoImagenCollection);
      });
    });

    describe('compareVideoImagen', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareVideoImagen(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 32451 };
        const entity2 = null;

        const compareResult1 = service.compareVideoImagen(entity1, entity2);
        const compareResult2 = service.compareVideoImagen(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 32451 };
        const entity2 = { id: 26692 };

        const compareResult1 = service.compareVideoImagen(entity1, entity2);
        const compareResult2 = service.compareVideoImagen(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 32451 };
        const entity2 = { id: 32451 };

        const compareResult1 = service.compareVideoImagen(entity1, entity2);
        const compareResult2 = service.compareVideoImagen(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
