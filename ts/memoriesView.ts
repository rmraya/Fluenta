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

class MemoriesView {

    electron = require('electron');

    container: HTMLDivElement;
    tableContainer: HTMLDivElement;
    tbody: HTMLTableSectionElement;
    memories: Memory[];
    memorySortAscending: boolean = true;
    memorySortFielD: string = 'description';
    selectedMemories: number[];
    appLanguage: string;

    constructor() {
        this.selectedMemories = [];
        this.container = document.getElementById('memoriesView') as HTMLDivElement;
        this.tbody = document.getElementById('memoriesTableBody') as HTMLTableSectionElement;
        this.tableContainer = document.getElementById('memoriesTableContainer') as HTMLDivElement;
        this.electron.ipcRenderer.on('import-tmx', () => {
            this.importTMX();
        });
        this.electron.ipcRenderer.on('export-tmx', () => {
            this.exportTMX();
        });
        document.getElementById('addMemory').addEventListener('click', () => {
            this.electron.ipcRenderer.send('show-add-memory');
        });
        document.getElementById('editMemory').addEventListener('click', () => {
            this.editMemory();
        });
        this.electron.ipcRenderer.on('edit-memory', () => {
            this.editMemory();
        });
        document.getElementById('selectAllMemories').addEventListener('click', () => {
            this.selectAllMemories((document.getElementById('selectAllMemories') as HTMLInputElement).checked);
        });
        document.getElementById('removeMemory').addEventListener('click', () => {
            this.removeMemories();
        });
        document.getElementById('importTMX').addEventListener('click', () => {
            this.importTMX();
        });
        document.getElementById('exportTMX').addEventListener('click', () => {
            this.exportTMX();
        });
        this.electron.ipcRenderer.send('get-app-language');
        this.electron.ipcRenderer.on('set-app-language', (event: any, language: string) => {
            this.appLanguage = language;
            this.electron.ipcRenderer.send('get-memories', 'MemoriesView');
        });
        this.electron.ipcRenderer.on('set-memories', (event: any, memories: Memory[]) => {
            this.memories = memories;
            this.displayMemories();
        });
        document.getElementById('memory-description').addEventListener('click', () => {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.memorySortFielD === 'description') {
                this.memorySortAscending = !this.memorySortAscending;
            } else {
                this.memorySortFielD = 'description';
                this.memorySortAscending = true;
            }
            this.displayMemories();
        });
        document.getElementById('memory-srcLang').addEventListener('click', () => {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.memorySortFielD === 'srcLang') {
                this.memorySortAscending = !this.memorySortAscending;
            } else {
                this.memorySortFielD = 'srcLang';
                this.memorySortAscending = true;
            }
            this.displayMemories();
        });
        document.getElementById('memory-created').addEventListener('click', () => {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.memorySortFielD === 'created') {
                this.memorySortAscending = !this.memorySortAscending;
            } else {
                this.memorySortFielD = 'created';
                this.memorySortAscending = true;
            }
            this.displayMemories();
        });
        document.getElementById('memory-updated').addEventListener('click', () => {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-down');
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.remove('arrow-up');
            if (this.memorySortFielD === 'updated') {
                this.memorySortAscending = !this.memorySortAscending;
            } else {
                this.memorySortFielD = 'updated';
                this.memorySortAscending = true;
            }
            this.displayMemories();
        });
        this.setSizes();
    }

    removeMemories(): void {
        if (this.selectedMemories.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectMemory' });
            return;
        }
        this.electron.ipcRenderer.send('remove-memories', this.selectedMemories);
        this.selectedMemories = [];
    }

    exportTMX(): void {
        if (this.selectedMemories.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectMemory' });
            return;
        }
        if (this.selectedMemories.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectOneMemory' });
            return;
        }
        let memory: Memory = this.memories.filter((value: Memory) => {
            return value.id === this.selectedMemories[0];
        })[0];
        this.electron.ipcRenderer.send('show-export-tmx', { id: memory.id, name: memory.name });
    }

    importTMX(): void {
        if (this.selectedMemories.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectMemory' });
            return;
        }
        if (this.selectedMemories.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectOneMemory' });
            return;
        }
        this.electron.ipcRenderer.send('show-import-tmx', this.selectedMemories[0]);
    }

    editMemory(): void {
        if (this.selectedMemories.length === 0) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectMemory' });
            return;
        }
        if (this.selectedMemories.length > 1) {
            this.electron.ipcRenderer.send('show-message', { type: 'warning', group: 'memoriesView', key: 'selectOneMemory' });
            return;
        }
        this.electron.ipcRenderer.send('edit-selected-memory', this.selectedMemories[0]);
    }

    displayMemories(): void {
        this.selectedMemories = [];
        if (this.memorySortAscending) {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.add('arrow-up');
        } else {
            (document.getElementById('memory-' + this.memorySortFielD) as HTMLTableCellElement).classList.add('arrow-down');
        }
        this.memories.sort((a: Memory, b: Memory) => {
            let x: string;
            let y: string;
            if (this.memorySortFielD === 'description') {
                x = a.name.toLocaleLowerCase(this.appLanguage);
                y = b.name.toLocaleLowerCase(this.appLanguage);
            } else if (this.memorySortFielD === 'srcLang') {
                x = a.srcLanguage;
                y = b.srcLanguage;
            } else if (this.memorySortFielD === 'created') {
                x = a.creationDate;
                y = b.creationDate;
            } else if (this.memorySortFielD === 'updated') {
                x = a.lastUpdate;
                y = b.lastUpdate;
            }
            if (x < y) { return -1; }
            if (x > y) { return 1; }
            return 0;
        });
        if (!this.memorySortAscending) {
            this.memories.reverse();
        }
        this.tbody.innerHTML = '';
        for (let memory of this.memories) {
            let row: HTMLTableRowElement = document.createElement('tr');
            this.tbody.appendChild(row);

            let cell: HTMLTableCellElement = document.createElement('td');
            cell.classList.add('center');
            let checkbox: HTMLInputElement = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = 'memory' + memory.id;
            checkbox.addEventListener('click', (event) => {
                if (checkbox.checked) {
                    this.selectedMemories.push(memory.id);
                } else {
                    this.selectedMemories = this.selectedMemories.filter((value: number) => {
                        return value !== memory.id;
                    });
                }
                event.stopPropagation();
            });
            checkbox.checked = this.selectedMemories.includes(memory.id);
            cell.appendChild(checkbox);
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.textContent = memory.name;
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = memory.srcLanguage;
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = memory.creationDate;
            row.appendChild(cell);

            cell = document.createElement('td');
            cell.classList.add('center');
            cell.textContent = memory.lastUpdate;
            row.appendChild(cell);

            row.addEventListener('click', () => {
                checkbox.checked = !checkbox.checked;
                if (checkbox.checked) {
                    this.selectedMemories.push(memory.id);
                } else {
                    this.selectedMemories = this.selectedMemories.filter((value: number) => {
                        return value !== memory.id;
                    });
                }
            });
        }
    }

    selectAllMemories(checked: boolean) {
        let checkboxes: NodeListOf<HTMLInputElement> = document.querySelectorAll('input[type="checkbox"]');
        this.selectedMemories = [];
        for (let checkbox of checkboxes) {
            if (checkbox.id.startsWith('memory')) {
                checkbox.checked = checked;
                if (checked) {
                    this.selectedMemories.push(parseInt(checkbox.id.substring(6)));
                }
            }
        }
    }

    hide(): void {
        this.container.classList.add('hidden');
    }

    show(): void {
        this.container.classList.remove('hidden');
    }

    setSizes(): void {
        let rightSide: HTMLDivElement = document.getElementById('rightSide') as HTMLDivElement;
        let memoriesTopBar: HTMLDivElement = document.getElementById('memoriesTopBar') as HTMLDivElement;
        this.tableContainer.style.height = (rightSide.clientHeight - memoriesTopBar.clientHeight) + 'px';
        this.tableContainer.style.width = (document.body.clientWidth - document.getElementById('leftSide').clientWidth) + 'px';
    }
}