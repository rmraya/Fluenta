/*******************************************************************************
 * Copyright (c) 2015-2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

class ProjectMemoriesDialog {

    electron = require('electron');

    memories: Memory[] = [];
    selectedMemories: number[] = [];

    constructor() {
        this.electron.ipcRenderer.send('get-theme');
        this.electron.ipcRenderer.on('set-theme', (event: Electron.IpcRendererEvent, theme: string) => {
            (document.getElementById('theme') as HTMLLinkElement).href = theme;
            this.electron.ipcRenderer.send('get-memories', 'ProjectMemoriesDialog');
        });
        this.electron.ipcRenderer.on('set-memories', (event: Electron.IpcRendererEvent, memories: Memory[]) => {
            this.memories = memories;
            this.electron.ipcRenderer.send('get-project-memories');
        });
        this.electron.ipcRenderer.on('set-project-memories', (event: Electron.IpcRendererEvent, memories: number[]) => {
            this.selectedMemories = memories;
            this.displayMemories();
        });
        document.getElementById('saveMemories').addEventListener('click', () => {
            this.saveMemories();
        });
        setTimeout(() => {
            this.electron.ipcRenderer.send('set-height', { window: 'projectMemoriesDialog', width: document.body.clientWidth, height: document.body.clientHeight });
        }, 300);
    }

    saveMemories() {
        this.electron.ipcRenderer.send('save-project-memories', this.selectedMemories);
    }

    displayMemories() {
        let tableBody: HTMLTableSectionElement = document.getElementById('tableBody') as HTMLTableSectionElement;
        tableBody.innerHTML = '';
        for (let memory of this.memories) {
            let row: HTMLTableRowElement = document.createElement('tr');
            tableBody.appendChild(row);
            let cell: HTMLTableCellElement = document.createElement('td');
            cell.style.width = '20px';
            cell.classList.add('middle');
            let checkBox: HTMLInputElement = document.createElement('input');
            checkBox.type = 'checkbox';
            checkBox.id = memory.id.toString();
            checkBox.checked = this.selectedMemories.includes(memory.id);
            cell.appendChild(checkBox);
            row.appendChild(cell);
            checkBox.addEventListener('change', () => {
                if (checkBox.checked) {
                    this.selectedMemories.push(memory.id);
                } else {
                    let index: number = this.selectedMemories.indexOf(memory.id);
                    if (index > -1) {
                        this.selectedMemories.splice(index, 1);
                    }
                }
            });
            cell = document.createElement('td');
            cell.classList.add('middle');
            let label: HTMLLabelElement = document.createElement('label');
            label.htmlFor = memory.id.toString();
            label.innerText = memory.name;
            cell.appendChild(label);
            row.appendChild(cell);
            checkBox.addEventListener('input', () => {
                if (checkBox.checked) {
                    this.selectedMemories.push(memory.id);
                } else {
                    let index: number = this.selectedMemories.indexOf(memory.id);
                    this.selectedMemories.splice(index, 1);
                }
            });
        }
    }
}
