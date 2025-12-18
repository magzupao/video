import { HttpResponse, provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';

import { TranslateModule } from '@ngx-translate/core';
import { Subject, from, of } from 'rxjs';

import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import { VideoCreditoService } from '../service/video-credito.service';
import { IVideoCredito } from '../video-credito.model';

import { VideoCreditoFormService } from './video-credito-form.service';
import { VideoCreditoUpdate } from './video-credito-update';

describe('VideoCredito Management Update Component', () => {
  let comp: VideoCreditoUpdate;
  let fixture: ComponentFixture<VideoCreditoUpdate>;
  let activatedRoute: ActivatedRoute;
  let videoCreditoFormService: VideoCreditoFormService;
  let videoCreditoService: VideoCreditoService;
  let userService: UserService;

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

    fixture = TestBed.createComponent(VideoCreditoUpdate);
    activatedRoute = TestBed.inject(ActivatedRoute);
    videoCreditoFormService = TestBed.inject(VideoCreditoFormService);
    videoCreditoService = TestBed.inject(VideoCreditoService);
    userService = TestBed.inject(UserService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('should call User query and add missing value', () => {
      const videoCredito: IVideoCredito = { id: 9058 };
      const user: IUser = { id: 3944 };
      videoCredito.user = user;

      const userCollection: IUser[] = [{ id: 3944 }];
      jest.spyOn(userService, 'query').mockReturnValue(of(new HttpResponse({ body: userCollection })));
      const additionalUsers = [user];
      const expectedCollection: IUser[] = [...additionalUsers, ...userCollection];
      jest.spyOn(userService, 'addUserToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ videoCredito });
      comp.ngOnInit();

      expect(userService.query).toHaveBeenCalled();
      expect(userService.addUserToCollectionIfMissing).toHaveBeenCalledWith(
        userCollection,
        ...additionalUsers.map(i => expect.objectContaining(i) as typeof i),
      );
      expect(comp.usersSharedCollection()).toEqual(expectedCollection);
    });

    it('should update editForm', () => {
      const videoCredito: IVideoCredito = { id: 9058 };
      const user: IUser = { id: 3944 };
      videoCredito.user = user;

      activatedRoute.data = of({ videoCredito });
      comp.ngOnInit();

      expect(comp.usersSharedCollection()).toContainEqual(user);
      expect(comp.videoCredito).toEqual(videoCredito);
    });
  });

  describe('save', () => {
    it('should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoCredito>>();
      const videoCredito = { id: 6706 };
      jest.spyOn(videoCreditoFormService, 'getVideoCredito').mockReturnValue(videoCredito);
      jest.spyOn(videoCreditoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoCredito });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: videoCredito }));
      saveSubject.complete();

      // THEN
      expect(videoCreditoFormService.getVideoCredito).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(videoCreditoService.update).toHaveBeenCalledWith(expect.objectContaining(videoCredito));
      expect(comp.isSaving).toEqual(false);
    });

    it('should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoCredito>>();
      const videoCredito = { id: 6706 };
      jest.spyOn(videoCreditoFormService, 'getVideoCredito').mockReturnValue({ id: null });
      jest.spyOn(videoCreditoService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoCredito: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: videoCredito }));
      saveSubject.complete();

      // THEN
      expect(videoCreditoFormService.getVideoCredito).toHaveBeenCalled();
      expect(videoCreditoService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVideoCredito>>();
      const videoCredito = { id: 6706 };
      jest.spyOn(videoCreditoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ videoCredito });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(videoCreditoService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareUser', () => {
      it('should forward to userService', () => {
        const entity = { id: 3944 };
        const entity2 = { id: 6275 };
        jest.spyOn(userService, 'compareUser');
        comp.compareUser(entity, entity2);
        expect(userService.compareUser).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
