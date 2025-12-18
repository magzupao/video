import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../video-credito.test-samples';

import { VideoCreditoFormService } from './video-credito-form.service';

describe('VideoCredito Form Service', () => {
  let service: VideoCreditoFormService;

  beforeEach(() => {
    service = TestBed.inject(VideoCreditoFormService);
  });

  describe('Service methods', () => {
    describe('createVideoCreditoFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVideoCreditoFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            videosConsumidos: expect.any(Object),
            videosDisponibles: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IVideoCredito should create a new form with FormGroup', () => {
        const formGroup = service.createVideoCreditoFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            videosConsumidos: expect.any(Object),
            videosDisponibles: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getVideoCredito', () => {
      it('should return NewVideoCredito for default VideoCredito initial value', () => {
        const formGroup = service.createVideoCreditoFormGroup(sampleWithNewData);

        const videoCredito = service.getVideoCredito(formGroup);

        expect(videoCredito).toMatchObject(sampleWithNewData);
      });

      it('should return NewVideoCredito for empty VideoCredito initial value', () => {
        const formGroup = service.createVideoCreditoFormGroup();

        const videoCredito = service.getVideoCredito(formGroup);

        expect(videoCredito).toMatchObject({});
      });

      it('should return IVideoCredito', () => {
        const formGroup = service.createVideoCreditoFormGroup(sampleWithRequiredData);

        const videoCredito = service.getVideoCredito(formGroup);

        expect(videoCredito).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVideoCredito should not enable id FormControl', () => {
        const formGroup = service.createVideoCreditoFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVideoCredito should disable id FormControl', () => {
        const formGroup = service.createVideoCreditoFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
