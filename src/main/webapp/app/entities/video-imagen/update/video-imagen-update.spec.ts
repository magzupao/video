import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { VideoService } from 'app/entities/video/service/video.service';
import { IVideo } from 'app/entities/video/video.model';
import { VideoImagenService } from '../service/video-imagen.service';
import { IVideoImagen } from '../video-imagen.model';

import { VideoImagenFormService } from './video-imagen-form.service';
import { VideoImagenUpdate } from './video-imagen-update';

describe('VideoImagen Management Update Component', () => {
  let comp: VideoImagenUpdate;
  let fixture: ComponentFixture<VideoImagenUpdate>;
  let activatedRoute: ActivatedRoute;
  let videoImagenFormService: VideoImagenFormService;
  let videoImagenService: VideoImagenService;
  let videoService: VideoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(VideoImagenUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    videoImagenFormService = TestBed.inject(VideoImagenFormService);
    videoImagenService = TestBed.inject(VideoImagenService);
    videoService = TestBed.inject(VideoService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call Video query and add missing value', () => {
      const videoImagen: IVideoImagen = { id: 26692 };
      const video: IVideo = { id: 10013 };
      videoImagen.video = video;

      const videoCollection: IVideo[] = [{ id: 10013 }];
      jest.spyOn(videoService, 'query').mockReturnValue(of(new HttpResponse({ body: videoCollection })));
      const additionalVideos = [video];
      const expectedCollection: IVideo[] = [...additionalVideos, ...videoCollection];
      jest.spyOn(videoService, 'addVideoToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ videoImagen });
      comp.ngOnInit();

      expect(videoService.query).toHaveBeenCalled();
      expect(videoService.addVideoToCollectionIfMissing).toHaveBeenCalledWith(
        videoCollection,
        ...additionalVideos.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.videosSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const videoImagen: IVideoImagen = { id: 26692 };
      const video: IVideo = { id: 10013 };
      videoImagen.video = video;

      activatedRoute.data = of({ videoImagen });
      comp.ngOnInit();

      expect(comp.videosSharedCollection()).toContainEqual(video);
      expect(comp.videoImagen).toEqual(videoImagen);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoImagen>>();
      const videoImagen = { id: 32451 };
      jest.spyOn(videoImagenFormService, 'getVideoImagen').mockReturnValue(videoImagen);
      jest.spyOn(videoImagenService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoImagen });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: videoImagen }));
      saveSubject.complete();

      // THEN
      expect(videoImagenFormService.getVideoImagen).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(videoImagenService.update).toHaveBeenCalledWith(expect.objectContaining(videoImagen));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoImagen>>();
      const videoImagen = { id: 32451 };
      jest.spyOn(videoImagenFormService, 'getVideoImagen').mockReturnValue({ id: null });
      jest.spyOn(videoImagenService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoImagen: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: videoImagen }));
      saveSubject.complete();

      // THEN
      expect(videoImagenFormService.getVideoImagen).toHaveBeenCalled();
      expect(videoImagenService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoImagen>>();
      const videoImagen = { id: 32451 };
      jest.spyOn(videoImagenService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoImagen });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(videoImagenService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareVideo', () => {
      it('should forward to videoService', () => {
        const entity = { id: 10013 };
        const entity2 = { id: 7071 };
        jest.spyOn(videoService, 'compareVideo');
        comp.compareVideo(entity, entity2);
        expect(videoService.compareVideo).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
