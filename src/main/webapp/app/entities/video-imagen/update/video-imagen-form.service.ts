import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IVideoImagen, NewVideoImagen } from '../video-imagen.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVideoImagen for edit and NewVideoImagenFormGroupInput for create.
 */
type VideoImagenFormGroupInput = IVideoImagen | PartialWithRequiredKeyOf<NewVideoImagen>;

type VideoImagenFormDefaults = Pick<NewVideoImagen, 'id'>;

type VideoImagenFormGroupContent = {
  id: FormControl<IVideoImagen['id'] | NewVideoImagen['id']>;
  filename: FormControl<IVideoImagen['filename']>;
  orden: FormControl<IVideoImagen['orden']>;
  duracionIndividual: FormControl<IVideoImagen['duracionIndividual']>;
  video: FormControl<IVideoImagen['video']>;
};

export type VideoImagenFormGroup = FormGroup<VideoImagenFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VideoImagenFormService {
  createVideoImagenFormGroup(videoImagen?: VideoImagenFormGroupInput): VideoImagenFormGroup {
    const videoImagenRawValue = {
      ...this.getFormDefaults(),
      ...(videoImagen ?? { id: null }),
    };
    return new FormGroup<VideoImagenFormGroupContent>({
      id: new FormControl(
        { value: videoImagenRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      filename: new FormControl(videoImagenRawValue.filename, {
        validators: [Validators.required, Validators.maxLength(255)],
      }),
      orden: new FormControl(videoImagenRawValue.orden, {
        validators: [Validators.required, Validators.min(0), Validators.max(9)],
      }),
      duracionIndividual: new FormControl(videoImagenRawValue.duracionIndividual),
      video: new FormControl(videoImagenRawValue.video, {
        validators: [Validators.required],
      }),
    });
  }

  getVideoImagen(form: VideoImagenFormGroup): IVideoImagen | NewVideoImagen {
    return form.getRawValue() as IVideoImagen | NewVideoImagen;
  }

  resetForm(form: VideoImagenFormGroup, videoImagen: VideoImagenFormGroupInput): void {
    const videoImagenRawValue = { ...this.getFormDefaults(), ...videoImagen };
    form.reset({
      ...videoImagenRawValue,
      id: { value: videoImagenRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): VideoImagenFormDefaults {
    return {
      id: null,
    };
  }
}
