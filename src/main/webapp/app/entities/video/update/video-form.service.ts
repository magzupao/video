import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IVideo, NewVideo } from '../video.model';
import { FormatoVideo } from 'app/entities/enumerations/formato-video.model'; // ✨ IMPORTAR

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVideo for edit and NewVideoFormGroupInput for create.
 */
type VideoFormGroupInput = IVideo | PartialWithRequiredKeyOf<NewVideo>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IVideo | NewVideo> = Omit<T, 'fechaCreacion' | 'fechaDescarga'> & {
  fechaCreacion?: string | null;
  fechaDescarga?: string | null;
};

type VideoFormRawValue = FormValueOf<IVideo>;

type NewVideoFormRawValue = FormValueOf<NewVideo>;

type VideoFormDefaults = Pick<NewVideo, 'id' | 'tieneAudio' | 'fechaCreacion' | 'fechaDescarga' | 'formato'>; // ✨ AGREGAR 'formato'

type VideoFormGroupContent = {
  id: FormControl<VideoFormRawValue['id'] | NewVideo['id']>;
  titulo: FormControl<VideoFormRawValue['titulo']>;
  audioFilename: FormControl<VideoFormRawValue['audioFilename']>;
  tieneAudio: FormControl<VideoFormRawValue['tieneAudio']>;
  duracionTransicion: FormControl<VideoFormRawValue['duracionTransicion']>;
  estado: FormControl<VideoFormRawValue['estado']>;
  fechaCreacion: FormControl<VideoFormRawValue['fechaCreacion']>;
  fechaDescarga: FormControl<VideoFormRawValue['fechaDescarga']>;
  user: FormControl<VideoFormRawValue['user']>;
  formato: FormControl<VideoFormRawValue['formato']>; // ✨ NUEVO CAMPO
};

export type VideoFormGroup = FormGroup<VideoFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VideoFormService {
  createVideoFormGroup(video?: VideoFormGroupInput): VideoFormGroup {
    const videoRawValue = this.convertVideoToVideoRawValue({
      ...this.getFormDefaults(),
      ...(video ?? { id: null }),
    });
    return new FormGroup<VideoFormGroupContent>({
      id: new FormControl(
        { value: videoRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [],
        },
      ),
      titulo: new FormControl(videoRawValue.titulo, {
        validators: [],
      }),
      audioFilename: new FormControl(videoRawValue.audioFilename, {
        validators: [],
      }),
      tieneAudio: new FormControl(videoRawValue.tieneAudio, {
        validators: [],
      }),
      duracionTransicion: new FormControl(videoRawValue.duracionTransicion),
      estado: new FormControl(videoRawValue.estado, {
        validators: [Validators.min(1)],
      }),
      fechaCreacion: new FormControl(videoRawValue.fechaCreacion, {
        validators: [],
      }),
      fechaDescarga: new FormControl(videoRawValue.fechaDescarga),
      user: new FormControl(videoRawValue.user, {
        validators: [],
      }),
      // ✨ NUEVO CONTROL DE FORMATO
      formato: new FormControl(videoRawValue.formato, {
        validators: [],
      }),
    });
  }

  getVideo(form: VideoFormGroup): IVideo | NewVideo {
    return this.convertVideoRawValueToVideo(form.getRawValue() as VideoFormRawValue | NewVideoFormRawValue);
  }

  resetForm(form: VideoFormGroup, video: VideoFormGroupInput): void {
    const videoRawValue = this.convertVideoToVideoRawValue({ ...this.getFormDefaults(), ...video });
    form.reset({
      ...videoRawValue,
      id: { value: videoRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): VideoFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      tieneAudio: false,
      fechaCreacion: currentTime,
      fechaDescarga: currentTime,
      formato: FormatoVideo.VERTICAL, // ✨ VALOR POR DEFECTO
    };
  }

  private convertVideoRawValueToVideo(rawVideo: VideoFormRawValue | NewVideoFormRawValue): IVideo | NewVideo {
    return {
      ...rawVideo,
      fechaCreacion: dayjs(rawVideo.fechaCreacion, DATE_TIME_FORMAT),
      fechaDescarga: dayjs(rawVideo.fechaDescarga, DATE_TIME_FORMAT),
    };
  }

  private convertVideoToVideoRawValue(
    video: IVideo | (Partial<NewVideo> & VideoFormDefaults),
  ): VideoFormRawValue | PartialWithRequiredKeyOf<NewVideoFormRawValue> {
    return {
      ...video,
      fechaCreacion: video.fechaCreacion ? video.fechaCreacion.format(DATE_TIME_FORMAT) : undefined,
      fechaDescarga: video.fechaDescarga ? video.fechaDescarga.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
