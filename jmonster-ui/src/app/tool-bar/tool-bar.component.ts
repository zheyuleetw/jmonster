import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-tool-bar',
  templateUrl: './tool-bar.component.html',
  styleUrls: ['./tool-bar.component.css']
})
export class ToolBarComponent implements OnInit {

  constructor() { }

  emoji: string = '💫'

  emojis = ['💫', '🍀', '🦆', '🦦', '🍉', '🌶', '🍤', '🦁', '🍊']

  ngOnInit(): void {
    setInterval(() => { this.emoji = this.currentMood(); }, 5000);
  }

  currentMood() {
    const luckyNumber = Math.floor(Math.random() * this.emojis.length);
    return this.emojis[luckyNumber]
  }

}
