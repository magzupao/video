import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IVideoCredito, NewVideoCredito } from '../video-credito.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVideoCredito for edit and NewVideoCreditoFormGroupInput for create.
 */
type VideoCreditoFormGroupInput = IVideoCredito | PartialWithRequiredKeyOf<NewVideoCredito>;

type VideoCreditoFormDefaults = Pick<NewVideoCredito, 'id'>;

type VideoCreditoFormGroupContent = {
  id: FormControl<IVideoCredito['id'] | NewVideoCredito['id']>;
  videosConsumidos: FormControl<IVideoCredito['videosConsumidos']>;
  videosDisponibles: FormControl<IVideoCredito['videosDisponibles']>;
  user: FormControl<IVideoCredito['user']>;
};

export type VideoCreditoFormGroup = FormGroup<VideoCreditoFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VideoCreditoFormService {
  createVideoCreditoFormGroup(videoCredito?: VideoCreditoFormGroupInput): VideoCreditoFormGroup {
    const videoCreditoRawValue = {
      ...this.getFormDefaults(),
      ...(videoCredito ?? { id: null }),
    };
    return new FormGroup<VideoCreditoFormGroupContent>({
      id: new FormControl(
        { value: videoCreditoRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      videosConsumidos: new FormControl(videoCreditoRawValue.videosConsumidos, {
        validators: [Validators.required, Validators.min(0)],
      }),
      videosDisponibles: new FormControl(videoCreditoRawValue.videosDisponibles, {
        validators: [Validators.required, Validators.min(0)],
      }),
      user: new FormControl(videoCreditoRawValue.user, {
        validators: [Validators.required],
      }),
    });
  }

  getVideoCredito(form: VideoCreditoFormGroup): IVideoCredito | NewVideoCredito {
    return form.getRawValue() as IVideoCredito | NewVideoCredito;
  }

  resetForm(form: VideoCreditoFormGroup, videoCredito: VideoCreditoFormGroupInput): void {
    const videoCreditoRawValue = { ...this.getFormDefaults(), ...videoCredito };
    form.reset({
      ...videoCreditoRawValue,
      id: { value: videoCreditoRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): VideoCreditoFormDefaults {
    return {
      id: null,
    };
  }
}
