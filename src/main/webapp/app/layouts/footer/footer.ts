import { Component } from '@angular/core';

import { TranslateDirective } from 'app/shared/language';

@Component({
  selector: 'jhi-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
  imports: [TranslateDirective],
})
export default class Footer {}
