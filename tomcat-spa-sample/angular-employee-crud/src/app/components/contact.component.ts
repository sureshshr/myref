import { Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <div class="card">
      <h2 style="margin-top:0;">Contact</h2>
      <div class="muted">Questions about this sample app?</div>

      <div style="margin-top: 14px;" class="row">
        <div class="card">
          <div class="muted">Support email</div>
          <div style="font-weight: 650;">support&#64;example.invalid</div>
        </div>

        <div class="card">
          <div class="muted">Notes</div>
          <div>
            This is a demo app. Employee data is stored locally in your browser.
          </div>
        </div>
      </div>
    </div>
  `
})
export class ContactComponent {}
