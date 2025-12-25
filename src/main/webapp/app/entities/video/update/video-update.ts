import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, OnDestroy, inject, signal, ChangeDetectorRef } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Observable, interval, Subscription, of } from 'rxjs'; // ✏️ AÑADIDO of
import { finalize, map, switchMap, takeWhile, timeout, catchError } from 'rxjs/operators'; // ✏️ AÑADIDO timeout, catchError

import { EstadoVideo } from 'app/entities/enumerations/estado-video.model';
import { FormatoVideo } from 'app/entities/enumerations/formato-video.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import SharedModule from 'app/shared/shared.module';
import { VideoService } from '../service/video.service';
import { IVideo } from '../video.model';
import { VideoCreditoService } from '../../video-credito/service/video-credito.service';
import { IVideoCredito } from '../../video-credito/video-credito.model';

import { VideoFormGroup, VideoFormService } from './video-form.service';

// Nueva interfaz para manejar las imágenes con preview
interface ImageWithPreview {
  file: File;
  preview: string;
}

@Component({
  selector: 'jhi-video-update',
  templateUrl: './video-update.html',
  styleUrls: ['./video-update.scss'],
  imports: [SharedModule, ReactiveFormsModule],
})
export class VideoUpdate implements OnInit, OnDestroy {
  isSaving = false;
  video: IVideo | null = null;
  estadoVideoValues = Object.keys(EstadoVideo);

  // Propiedades para imágenes
  selectedImages: File[] = [];
  imagesWithPreview: ImageWithPreview[] = [];
  imagesError: string | null = null;

  // Propiedades para audio
  selectedAudio: File | null = null;
  audioError: string | null = null;

  // 🆕 Propiedades para polling
  private pollingSubscription: Subscription | null = null;
  isProcessing = false;
  processingMessage = 'Guardando video, audio e imágenes...';
  videoEstado: string | null = null;

  usersSharedCollection = signal<IUser[]>([]);

  generatedVideoId: number | null = null;
  downloadUrl: string | null = null;
  outputFilename: string | null = null;
  isDownloading = false;
  lastPolledVideo: IVideo | null = null;

  formatoVideoValues = Object.keys(FormatoVideo);

  // ⭐ NUEVAS PROPIEDADES PARA CRÉDITOS
  userCredits: IVideoCredito | null = null;
  loadingCredits = true;
  creditsError = false;
  hasAvailableCredits = false;

  protected cdr = inject(ChangeDetectorRef);
  protected videoService = inject(VideoService);
  protected videoCreditoService = inject(VideoCreditoService);
  protected videoFormService = inject(VideoFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VideoFormGroup = this.videoFormService.createVideoFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    console.log('🚀 VideoUpdate ngOnInit - Iniciando componente');

    this.activatedRoute.data.subscribe(({ video }) => {
      this.video = video;
      console.log('📹 Video desde ruta:', video);

      if (video) {
        this.updateForm(video);
      }

      this.loadRelationshipsOptions();
    });

    // ⭐ CARGAR CRÉDITOS AL INICIAR
    console.log('💳 Iniciando carga de créditos...');
    this.loadUserCredits();

    // ✨ SUSCRIBIRSE A CAMBIOS DEL CHECKBOX tieneAudio
    this.editForm.controls.tieneAudio.valueChanges.subscribe(tieneAudio => {
      console.log('🔄 Cambio en tieneAudio:', tieneAudio);

      if (tieneAudio) {
        const duracionActual = this.editForm.controls.duracionTransicion.value;
        if (duracionActual === null || duracionActual === undefined) {
          this.editForm.patchValue({ duracionTransicion: 5 });
          console.log('⏱️ Duración establecida a 5 segundos (por defecto)');
        }
      } else {
        this.editForm.patchValue({ duracionTransicion: null });
        console.log('⏱️ Duración limpiada');
      }
    });
  }

  // ⭐ MÉTODO PARA CARGAR CRÉDITOS (MEJORADO CON LOGS Y TIMEOUT)
  loadUserCredits(): void {
    this.loadingCredits = true;
    this.creditsError = false;

    console.log('🔄 Iniciando carga de créditos del usuario...');
    console.log('📍 Estado inicial:', {
      loadingCredits: this.loadingCredits,
      creditsError: this.creditsError,
      hasAvailableCredits: this.hasAvailableCredits,
    });

    this.videoCreditoService
      .getCurrentUserCredits()
      .pipe(
        timeout(10000), // ⏱️ Timeout de 10 segundos
        catchError(error => {
          console.error('❌ Error capturado en pipe:', error);
          if (error.name === 'TimeoutError') {
            console.error('⏰ Timeout: La petición tardó más de 10 segundos');
          }
          return of(null); // Retornar observable con null
        }),
      )
      .subscribe({
        next: (res: HttpResponse<IVideoCredito> | null) => {
          console.log('✅ Respuesta recibida del servicio:', res);

          if (res && res.body) {
            this.userCredits = res.body;
            console.log('💰 Créditos del usuario cargados:', {
              id: this.userCredits.id,
              videosConsumidos: this.userCredits.videosConsumidos,
              videosDisponibles: this.userCredits.videosDisponibles,
              user: this.userCredits.user,
            });

            this.checkAvailableCredits();
          } else {
            console.warn('⚠️ Respuesta vacía o sin body');
            console.warn('⚠️ No se encontraron créditos para el usuario');
            this.creditsError = true;
            this.hasAvailableCredits = false;
          }

          this.loadingCredits = false;
          console.log('✔️ loadingCredits = false');
          this.cdr.detectChanges(); // Forzar detección de cambios
        },
        error: error => {
          console.error('💥 Error en la suscripción (método error):', error);
          console.error('📊 Detalles del error:', {
            name: error.name,
            message: error.message,
            status: error.status,
            statusText: error.statusText,
            url: error.url,
          });

          this.creditsError = true;
          this.loadingCredits = false;
          this.hasAvailableCredits = false;
          this.disableForm();

          console.log('❌ Estado final después del error:', {
            loadingCredits: this.loadingCredits,
            creditsError: this.creditsError,
            hasAvailableCredits: this.hasAvailableCredits,
          });

          this.cdr.detectChanges(); // Forzar detección de cambios
        },
        complete: () => {
          console.log('🏁 Observable de créditos completado');
          this.loadingCredits = false;
          console.log('📊 Estado final:', {
            loadingCredits: this.loadingCredits,
            creditsError: this.creditsError,
            hasAvailableCredits: this.hasAvailableCredits,
            userCredits: this.userCredits,
          });
          this.cdr.detectChanges(); // Forzar detección de cambios
        },
      });
  }

  checkAvailableCredits(): void {
    console.log('🔍 Verificando créditos disponibles...');

    if (!this.userCredits) {
      console.warn('⚠️ No hay créditos cargados');
      this.hasAvailableCredits = false;
      this.disableForm();
      return;
    }

    const consumidos = this.userCredits.videosConsumidos ?? 0;
    const disponibles = this.userCredits.videosDisponibles ?? 0;

    console.log('📊 Análisis de créditos:', {
      consumidos,
      disponibles,
      restantes: disponibles - consumidos,
    });

    this.hasAvailableCredits = consumidos < disponibles;

    console.log(`${this.hasAvailableCredits ? '✅' : '❌'} hasAvailableCredits = ${this.hasAvailableCredits}`);

    if (!this.hasAvailableCredits) {
      console.warn('⚠️ Sin créditos disponibles - Deshabilitando formulario');
      this.disableForm();
    } else {
      console.log('✅ Créditos disponibles - Formulario habilitado');
    }
  }

  // ⭐ DESHABILITAR FORMULARIO
  disableForm(): void {
    console.log('🔒 Deshabilitando formulario completo');
    this.editForm.disable();
  }

  // ⭐ CALCULAR CRÉDITOS RESTANTES
  getRemainingCredits(): number {
    if (!this.userCredits) {
      console.log('💳 getRemainingCredits: No hay créditos cargados, retornando 0');
      return 0;
    }
    const consumidos = this.userCredits.videosConsumidos ?? 0;
    const disponibles = this.userCredits.videosDisponibles ?? 0;
    const restantes = Math.max(0, disponibles - consumidos);

    console.log('💰 Créditos restantes calculados:', restantes);
    return restantes;
  }

  // 🆕 NUEVO MÉTODO: Cleanup al destruir el componente
  ngOnDestroy(): void {
    console.log('🧹 VideoUpdate ngOnDestroy - Limpiando componente');
    this.stopPolling();
  }

  previousState(): void {
    console.log('⬅️ Regresando a la página anterior');
    globalThis.history.back();
  }

  // ✏️ MODIFICADO: Método save() completo
  save(): void {
    console.log('💾 Iniciando guardado del video...');

    // ⭐ VALIDAR CRÉDITOS ANTES DE GUARDAR
    if (!this.hasAvailableCredits) {
      console.error('❌ Sin créditos disponibles');
      alert('No tienes créditos disponibles para crear videos.');
      return;
    }

    console.log('✅ Validación de créditos OK');

    this.isSaving = true;
    this.isProcessing = true;
    this.processingMessage = 'Guardando video...';

    this.editForm.disable();

    if (!this.isImagesValid()) {
      console.error('❌ Validación de imágenes falló');
      this.isSaving = false;
      this.isProcessing = false;
      this.editForm.enable();
      this.imagesError = this.imagesError ?? 'Selecciona entre 1 y 10 imágenes.';
      return;
    }

    console.log('✅ Validación de imágenes OK');

    const tieneAudio = this.editForm.controls.tieneAudio.value;
    const tieneArchivoAudio = this.selectedAudio !== null;

    console.log('🎵 Validación de audio:', { tieneAudio, tieneArchivoAudio });

    if (!tieneArchivoAudio && !tieneAudio) {
      console.error('❌ Validación de audio falló');
      this.audioError = 'Debes subir un audio O marcar la opción "sin audio" y definir la duración de cada imagen.';
      this.isSaving = false;
      this.isProcessing = false;
      this.editForm.enable();
      return;
    }

    console.log('✅ Validación de audio OK');

    const video = this.videoFormService.getVideo(this.editForm);
    console.log('📹 Video a guardar:', video);

    if (video.id === null) {
      console.log('🆕 Creando nuevo video...');
      this.videoService.create(video, this.selectedImages, this.selectedAudio).subscribe({
        next: response => {
          console.log('✅ Respuesta del servidor:', response);

          if (response.status === 202 && response.body) {
            const createdVideo = response.body;
            console.log('🚀 Video creado con ID:', createdVideo.id);
            console.log('⏳ Estado inicial:', createdVideo.estado);

            this.generatedVideoId = createdVideo.id ?? null;

            this.processingMessage = 'Generando video... esto puede tardar varios minutos';
            this.videoEstado = createdVideo.estado ?? null;

            this.startPolling(createdVideo.id!);
          } else {
            console.warn('⚠️ Respuesta inesperada del servidor:', response.status);
            this.onSaveError();
          }
        },
        error: error => {
          console.error('❌ Error creando video:', error);
          this.onSaveError();
        },
      });
    } else {
      console.log('✏️ Actualizando video existente...');
      this.subscribeToSaveResponse(this.videoService.update(video, this.selectedImages, this.selectedAudio));
    }
  }

  get mostrarDuracionTransicion(): boolean {
    return this.editForm.controls.tieneAudio.value === true;
  }

  // 🆕 NUEVO MÉTODO: Inicia el polling para verificar el estado del video
  private startPolling(videoId: number): void {
    console.log('🔄 Iniciando polling para video ID:', videoId);

    this.pollingSubscription = interval(3000)
      .pipe(
        switchMap(() => {
          console.log('🔍 Consultando estado del video...');
          return this.videoService.getVideoStatus(videoId);
        }),
        takeWhile(response => {
          const video = response.body;
          if (!video) {
            console.warn('⚠️ No se recibió información del video');
            return false;
          }

          const estado = video.estado;
          this.videoEstado = estado ?? null;
          this.lastPolledVideo = video;
          this.generatedVideoId = video.id ?? videoId;

          console.log('📊 Estado actual:', estado);

          if (estado === 'EN_PROCESO') {
            this.processingMessage = 'Generando video... esto puede tardar varios minutos';
            return true;
          } else if (estado === 'COMPLETADO') {
            this.processingMessage = '✅ Video generado exitosamente!';
            this.downloadUrl = (video as any).downloadUrl ?? `/api/videos/${video.id ?? videoId}/download`;
            this.outputFilename = (video as any).outputFilename ?? null;
            return false;
          } else if (estado === 'ERROR') {
            this.processingMessage = '❌ Error generando el video';
            return false;
          }

          return true;
        }, true),
        finalize(() => {
          console.log('🏁 Polling finalizado');
          this.onPollingComplete();
          this.onSaveFinalize();
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: response => {
          const video = response.body;
          if (video) {
            console.log('📦 Video recibido en polling:', video);
          }
        },
        error: error => {
          console.error('❌ Error en polling:', error);
          this.onSaveError();
        },
      });
  }

  private onPollingComplete(): void {
    console.log('✅ Procesamiento completado');

    if (this.videoEstado === 'COMPLETADO') {
      if (!this.downloadUrl && this.lastPolledVideo?.id) {
        this.downloadUrl = (this.lastPolledVideo as any).downloadUrl ?? `/api/videos/${this.lastPolledVideo.id}/download`;
      }
      if (!this.outputFilename) {
        this.outputFilename = (this.lastPolledVideo as any).outputFilename ?? null;
      }

      this.onSaveSuccess();
    } else if (this.videoEstado === 'ERROR') {
      this.onSaveError();
    }
  }

  private stopPolling(): void {
    if (this.pollingSubscription) {
      console.log('⏹️ Deteniendo polling');
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  onAudioSelected(event: Event): void {
    console.log('🎵 Audio seleccionado');
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    console.log('📁 Archivo de audio:', file.name, file.size, file.type);

    const validAudioTypes = ['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg', 'audio/mp4', 'audio/x-m4a'];
    const validExtensions = ['.mp3', '.wav', '.ogg', '.m4a'];

    const isValidType = validAudioTypes.includes(file.type);
    const hasValidExtension = validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));

    if (!isValidType && !hasValidExtension) {
      console.error('❌ Tipo de audio no válido');
      this.audioError = 'Solo se permiten archivos de audio (MP3, WAV, OGG, M4A).';
      input.value = '';
      return;
    }

    const maxSize = 50 * 1024 * 1024;
    if (file.size > maxSize) {
      console.error('❌ Audio demasiado grande');
      this.audioError = 'El archivo de audio no debe superar los 50MB.';
      input.value = '';
      return;
    }

    this.selectedAudio = file;
    this.audioError = null;
    console.log('✅ Audio válido cargado');

    this.editForm.patchValue({
      audioFilename: file.name,
    });

    if (this.editForm.controls.tieneAudio.value) {
      this.editForm.patchValue({
        tieneAudio: false,
      });
      console.log('🔄 Checkbox "sin audio" desmarcado automáticamente');
    }

    input.value = '';
  }

  removeAudio(): void {
    console.log('🗑️ Eliminando audio');
    this.selectedAudio = null;
    this.audioError = null;

    this.editForm.patchValue({
      audioFilename: null,
    });
  }

  onImagesSelected(event: Event): void {
    console.log('🖼️ Imágenes seleccionadas');
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);
    console.log('📁 Archivos recibidos:', files.length);

    if (files.some(f => !f.type.startsWith('image/'))) {
      console.error('❌ Archivo no válido detectado');
      this.imagesError = 'Solo se permiten archivos de imagen.';
      input.value = '';
      return;
    }

    const totalImages = this.selectedImages.length + files.length;
    console.log('📊 Total de imágenes:', totalImages);

    if (totalImages > 10) {
      const available = 10 - this.selectedImages.length;
      if (available > 0) {
        const filesToAdd = files.slice(0, available);
        this.addFilesWithPreviews(filesToAdd);
        this.imagesError = `Solo puedes agregar ${available} imagen(es) más. Máximo 10 en total.`;
      } else {
        this.imagesError = 'Ya tienes 10 imágenes. No puedes agregar más.';
      }
    } else {
      this.addFilesWithPreviews(files);
      this.imagesError = null;
      console.log('✅ Imágenes agregadas correctamente');
    }

    input.value = '';
  }

  private addFilesWithPreviews(files: File[]): void {
    console.log('📸 Generando previews para', files.length, 'archivo(s)');

    files.forEach(file => {
      this.selectedImages.push(file);

      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.imagesWithPreview = [
          ...this.imagesWithPreview,
          {
            file: file,
            preview: e.target?.result as string,
          },
        ];
        console.log('✅ Preview generado para:', file.name);
        this.cdr.markForCheck();
      };
      reader.readAsDataURL(file);
    });
  }

  removeImage(index: number): void {
    console.log('🗑️ Eliminando imagen en índice:', index);
    this.selectedImages.splice(index, 1);
    this.imagesWithPreview.splice(index, 1);

    if (this.selectedImages.length === 0) {
      this.imagesError = 'Debes seleccionar al menos 1 imagen.';
    } else if (this.selectedImages.length <= 10) {
      this.imagesError = null;
    }

    console.log('📊 Imágenes restantes:', this.selectedImages.length);
  }

  isImagesValid(): boolean {
    const valid = this.selectedImages.length >= 1 && this.selectedImages.length <= 10 && !this.imagesError;
    console.log('🔍 Validación de imágenes:', valid);
    return valid;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  downloadGeneratedVideo(): void {
    if (!this.generatedVideoId || this.isDownloading) return;

    console.log('⬇️ Descargando video ID:', this.generatedVideoId);
    this.isDownloading = true;

    this.videoService.downloadVideo(this.generatedVideoId).subscribe({
      next: res => {
        const blob = res.body;
        if (!blob) {
          console.error('❌ No se recibió blob del video');
          this.isDownloading = false;
          return;
        }

        const cd = res.headers.get('content-disposition');
        const headerName = cd?.match(/filename="(.+?)"/)?.[1] ?? null;

        const filename = headerName ?? this.outputFilename ?? 'video.mp4';
        console.log('📥 Descargando como:', filename);

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        this.isDownloading = false;
        console.log('✅ Descarga completada');
      },
      error: err => {
        console.error('❌ Error descargando video', err);
        this.isDownloading = false;
      },
    });
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVideo>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    console.log('✅ Guardado exitoso');
  }

  protected onSaveError(): void {
    console.error('❌ Error en guardado');
  }

  protected onSaveFinalize(): void {
    console.log('🏁 Finalizando guardado');
    this.isSaving = false;
    this.isProcessing = false;
    this.editForm.enable();
    this.stopPolling();
  }

  protected updateForm(video: IVideo): void {
    console.log('✏️ Actualizando formulario con video:', video);
    this.video = video;
    this.videoFormService.resetForm(this.editForm, video);

    this.usersSharedCollection.set(this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection(), video.user));
  }

  protected loadRelationshipsOptions(): void {
    console.log('🔗 Cargando opciones de relaciones (users)');
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.video?.user)))
      .subscribe((users: IUser[]) => {
        console.log('👥 Usuarios cargados:', users.length);
        this.usersSharedCollection.set(users);
      });
  }
}
