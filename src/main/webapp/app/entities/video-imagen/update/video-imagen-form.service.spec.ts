import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../video-imagen.test-samples';

import { VideoImagenFormService } from './video-imagen-form.service';

describe('VideoImagen Form Service', () => {
  let service: VideoImagenFormService;

  beforeEach(() => {
    service = TestBed.inject(VideoImagenFormService);
  });

  describe('Service methods', () => {
    describe('createVideoImagenFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVideoImagenFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            filename: expect.any(Object),
            orden: expect.any(Object),
            duracionIndividual: expect.any(Object),
            video: expect.any(Object),
          }),
        );
      });

      it('passing IVideoImagen should create a new form with FormGroup', () => {
        const formGroup = service.createVideoImagenFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            filename: expect.any(Object),
            orden: expect.any(Object),
            duracionIndividual: expect.any(Object),
            video: expect.any(Object),
          }),
        );
      });
    });

    describe('getVideoImagen', () => {
      it('should return NewVideoImagen for default VideoImagen initial value', () => {
        const formGroup = service.createVideoImagenFormGroup(sampleWithNewData);

        const videoImagen = service.getVideoImagen(formGroup);

        expect(videoImagen).toMatchObject(sampleWithNewData);
      });

      it('should return NewVideoImagen for empty VideoImagen initial value', () => {
        const formGroup = service.createVideoImagenFormGroup();

        const videoImagen = service.getVideoImagen(formGroup);

        expect(videoImagen).toMatchObject({});
      });

      it('should return IVideoImagen', () => {
        const formGroup = service.createVideoImagenFormGroup(sampleWithRequiredData);

        const videoImagen = service.getVideoImagen(formGroup);

        expect(videoImagen).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVideoImagen should not enable id FormControl', () => {
        const formGroup = service.createVideoImagenFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVideoImagen should disable id FormControl', () => {
        const formGroup = service.createVideoImagenFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
