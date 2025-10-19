const CONFIG = {
    CANVAS: {
        WIDTH: 400,
        HEIGHT: 400,
        SCALE: 40,
        AXIS_COLOR: '#34363d',
        REGION_COLOR: 'rgba(100, 149, 237, 0.5)',
        HIT_COLOR: 'green',
        MISS_COLOR: 'red',
        POINT_RADIUS: 5,
        MARK_LENGTH: 10
    },
    VALIDATION: {
        Y_MIN: -5,
        Y_MAX: 3,
        X_VALUES: [-4, -3, -2, -1, 0, 1, 2, 3, 4]
    },
    SELECTORS: {
        CANVAS: 'coordinatePlane',
        X_SELECT: 'pointForm:xValue',
        Y_INPUT: 'pointForm:yValue_input',
        R_SELECT: 'pointForm:rValue',
        FORM: 'pointForm',
        R_INPUT: 'pointForm:rValue_input',
        RESULTS_TABLE: 'resultsTable'
    },
    TEXT: {
        VALIDATION: {
            NUMBER_REQUIRED: 'Y должно быть числом',
            RANGE_REQUIRED: 'Y должен быть строго в диапазоне (-5; 3)'
        }
    },
    TIMING: {
        TOOLTIP_SHOW: 10,
        TOOLTIP_HIDE: 3000
    }
};

const Utils = {
    roundToNearest(value, allowedValues) {
        let closest = allowedValues[0];
        let minDist = Math.abs(value - closest);

        for (const v of allowedValues) {
            const dist = Math.abs(value - v);
            if (dist < minDist) {
                minDist = dist;
                closest = v;
            }
        }
        return closest;
    },

    isInRange(value, min, max) {
        return value > min && value < max;
    },

    debounce(func, delay) {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), delay);
        };
    },

    parseResultsFromTable() {
        const results = [];
        const table = document.getElementById(CONFIG.SELECTORS.RESULTS_TABLE);

        if (!table) return results;

        const rows = table.querySelectorAll('tr:not(.ui-datatable-header)');

        rows.forEach(row => {
            const cells = row.querySelectorAll('td');
            if (cells.length >= 5) {
                try {
                    const result = {
                        x: parseFloat(cells[0].textContent.trim()),
                        y: parseFloat(cells[1].textContent.trim()),
                        r: parseFloat(cells[2].textContent.trim()),
                        hit: cells[3].textContent.trim().toLowerCase() === 'hit',
                        timestamp: cells[4].textContent.trim()
                    };

                    if (!isNaN(result.x) && !isNaN(result.y) && !isNaN(result.r)) {
                        results.push(result);
                    }
                } catch (e) {
                    console.warn('Error parsing table row:', e);
                }
            }
        });

        return results;
    }
};

class FormValidator {
    constructor() {
        this.yInput = document.getElementById(CONFIG.SELECTORS.Y_INPUT);
        this.rInput = document.getElementById(CONFIG.SELECTORS.R_INPUT);
        this.setupEventListeners();
    }

    setupEventListeners() {
        const debouncedY = Utils.debounce(this.validateY.bind(this), 300);
        const debouncedR = Utils.debounce(this.validateR.bind(this), 300);
        const form = document.getElementById(CONFIG.SELECTORS.FORM);

        if (this.yInput) {
            this.yInput.addEventListener('input', debouncedY);
            this.yInput.addEventListener('blur', () => this.validateY());
        }

        if (this.rInput) {
            this.rInput.addEventListener('input', debouncedR);
            this.rInput.addEventListener('blur', () => this.validateR());
        }

        if (form) {
            form.addEventListener('submit', (e) => {
                if (!this.validateY() || !this.validateR()) {
                    e.preventDefault();
                }
            });
        }
    }

    validateR() {
        const input = this.rInput;
        if (!input) return true;

        const value = input.value.trim();
        const container = input.closest('.input-container');
        this.removeTooltip(container);

        if (value === '') {
            input.classList.remove('input-error');
            return true;
        }

        if (!/^-?\d+$/.test(value) || isNaN(parseInt(value, 10))) {
            this.showError(container, 'R должно быть целым числом');
            return false;
        }

        const num = parseInt(value, 10);
        if (num < 1 || num > 4) {
            this.showError(container, 'R должен быть от 1 до 4');
            return false;
        }

        input.classList.remove('input-error');
        return true;
    }

    validateY() {
        const input = this.yInput;
        if (!input) return true;

        const value = input.value.trim();
        const container = input.closest('.input-container');
        this.removeTooltip(container);

        if (value === '') {
            input.classList.remove('input-error');
            return true;
        }

        if (!/^-?\d*\.?\d*$/.test(value) || isNaN(parseFloat(value))) {
            this.showError(container, CONFIG.TEXT.VALIDATION.NUMBER_REQUIRED);
            return false;
        }

        const num = parseFloat(value);
        if (!Utils.isInRange(num, CONFIG.VALIDATION.Y_MIN, CONFIG.VALIDATION.Y_MAX)) {
            this.showError(container, CONFIG.TEXT.VALIDATION.RANGE_REQUIRED);
            return false;
        }

        input.classList.remove('input-error');
        return true;
    }

    showError(container, message) {
        const input = this.yInput || this.rInput;
        if (input) input.classList.add('input-error');

        const tooltip = document.createElement('div');
        tooltip.className = 'input-tooltip';
        tooltip.textContent = message;
        container.appendChild(tooltip);

        setTimeout(() => tooltip.classList.add('show'), CONFIG.TIMING.TOOLTIP_SHOW);
        setTimeout(() => this.removeTooltip(tooltip), CONFIG.TIMING.TOOLTIP_HIDE);
    }

    removeTooltip(container) {
        const tooltip = container.querySelector('.input-tooltip');
        if (tooltip) {
            tooltip.classList.remove('show');
            setTimeout(() => {
                if (tooltip.parentNode) {
                    tooltip.parentNode.removeChild(tooltip);
                }
            }, 300);
        }
    }
}

class CoordinatePlane {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        if (!this.canvas) return;

        this.ctx = this.canvas.getContext('2d');
        this.centerX = CONFIG.CANVAS.WIDTH / 2;
        this.centerY = CONFIG.CANVAS.HEIGHT / 2;
        this.scale = CONFIG.CANVAS.SCALE;
        this.setup();
    }

    setup() {
        this.canvas.width = CONFIG.CANVAS.WIDTH;
        this.canvas.height = CONFIG.CANVAS.HEIGHT;
        this.canvas.addEventListener('click', this.handleClick.bind(this));
        this.draw();
    }

    handleClick(e) {
        const rect = this.canvas.getBoundingClientRect();
        const xCanvas = e.clientX - rect.left;
        const yCanvas = e.clientY - rect.top;
        const xGraph = (xCanvas - this.centerX) / this.scale;
        const yGraph = -(yCanvas - this.centerY) / this.scale;

        const roundedX = Utils.roundToNearest(xGraph, CONFIG.VALIDATION.X_VALUES);
        const yValue = parseFloat(yGraph.toFixed(2));

        if (isNaN(roundedX) || isNaN(yValue)) return;

        if (!Utils.isInRange(yValue, CONFIG.VALIDATION.Y_MIN, CONFIG.VALIDATION.Y_MAX)) {
            alert('Y должен быть строго в диапазоне (-5; 3)');
            return;
        }

        const rInput = document.getElementById('pointForm:rValue_input');
        const rVal = rInput ? parseFloat(rInput.value) : NaN;
        if (isNaN(rVal) || rVal < 1 || rVal > 4) {
            alert('Сначала выберите корректное R (1..4)');
            return;
        }

        const xWidget = PrimeFaces.getWidgetById('pointForm:xValue');
        const yWidget = PrimeFaces.getWidgetById('pointForm:yValue');
        const rWidget = PrimeFaces.getWidgetById('pointForm:rValue');

        if (xWidget) xWidget.setValue(roundedX);
        if (yWidget) yWidget.setValue(yValue);
        if (rWidget) rWidget.setValue(rVal);

        const xHidden = document.getElementById('canvasClickForm:canvasX');
        const yHidden = document.getElementById('canvasClickForm:canvasY');
        const rHidden = document.getElementById('canvasClickForm:canvasR');
        const submitBtn = document.getElementById('canvasClickForm:submitCanvasClick');

        if (xHidden && yHidden && rHidden && submitBtn) {
            xHidden.value = roundedX;
            yHidden.value = yValue;
            rHidden.value = rVal;
            submitBtn.click();
        } else {
            console.error('Canvas form elements not found!');
        }
    }

    draw() {
        if (!this.ctx) return;

        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawAxes();

        const rInput = document.getElementById(CONFIG.SELECTORS.R_INPUT);
        const currentR = rInput ? parseFloat(rInput.value) : NaN;

        if (!isNaN(currentR) && currentR >= 1 && currentR <= 4) {
            this.drawRegion(currentR);
            this.drawHistoryPoints();
        }
    }

    drawAxes() {
        const { AXIS_COLOR, MARK_LENGTH } = CONFIG.CANVAS;
        this.ctx.strokeStyle = AXIS_COLOR;
        this.ctx.lineWidth = 2;

        this.ctx.beginPath();
        this.ctx.moveTo(0, this.centerY);
        this.ctx.lineTo(this.canvas.width, this.centerY);
        this.ctx.moveTo(this.centerX, 0);
        this.ctx.lineTo(this.centerX, this.canvas.height);
        this.ctx.stroke();

        this.ctx.fillStyle = AXIS_COLOR;
        this.ctx.beginPath();
        this.ctx.moveTo(this.canvas.width - 10, this.centerY);
        this.ctx.lineTo(this.canvas.width, this.centerY - 5);
        this.ctx.lineTo(this.canvas.width, this.centerY + 5);
        this.ctx.closePath();
        this.ctx.fill();

        this.ctx.beginPath();
        this.ctx.moveTo(this.centerX, 10);
        this.ctx.lineTo(this.centerX - 5, 0);
        this.ctx.lineTo(this.centerX + 5, 0);
        this.ctx.closePath();
        this.ctx.fill();

        this.drawAxisMarks();
    }

    drawAxisMarks() {
        const { MARK_LENGTH, AXIS_COLOR } = CONFIG.CANVAS;
        const step = this.scale;

        for (let x = 0; x <= this.canvas.width; x += step) {
            if (x === this.centerX) continue;

            this.ctx.beginPath();
            this.ctx.moveTo(x, this.centerY - MARK_LENGTH / 2);
            this.ctx.lineTo(x, this.centerY + MARK_LENGTH / 2);
            this.ctx.stroke();

            const label = ((x - this.centerX) / this.scale).toFixed(1);
            if (Math.abs(parseFloat(label)) % 1 === 0) {
                this.ctx.fillStyle = AXIS_COLOR;
                this.ctx.font = '12px Arial';
                this.ctx.fillText(label, x - 8, this.centerY + 20);
            }
        }

        for (let y = 0; y <= this.canvas.height; y += step) {
            if (y === this.centerY) continue;

            this.ctx.beginPath();
            this.ctx.moveTo(this.centerX - MARK_LENGTH / 2, y);
            this.ctx.lineTo(this.centerX + MARK_LENGTH / 2, y);
            this.ctx.stroke();

            const label = ((this.centerY - y) / this.scale).toFixed(1);
            if (Math.abs(parseFloat(label)) % 1 === 0) {
                this.ctx.fillStyle = AXIS_COLOR;
                this.ctx.font = '12px Arial';
                this.ctx.fillText(label, this.centerX + 10, y + 5);
            }
        }
    }

    drawRegion(r) {
        const s = this.scale;
        const ctx = this.ctx;
        ctx.fillStyle = CONFIG.CANVAS.REGION_COLOR;

        ctx.beginPath();
        ctx.arc(this.centerX, this.centerY, r * s, Math.PI / 2, Math.PI, false);
        ctx.lineTo(this.centerX, this.centerY);
        ctx.closePath();
        ctx.fill();

        const leftRectX = this.centerX - (r / 2) * s;
        const leftRectY = this.centerY - r * s;
        ctx.fillRect(leftRectX, leftRectY, (r / 2) * s, r * s);

        ctx.beginPath();
        ctx.moveTo(this.centerX, this.centerY);
        ctx.lineTo(this.centerX + r * s, this.centerY);
        ctx.lineTo(this.centerX, this.centerY - (r / 2) * s);
        ctx.closePath();
        ctx.fill();
    }

    drawHistoryPoints() {
        const currentResults = Utils.parseResultsFromTable();
        if (currentResults.length === 0) return;

        const rInput = document.getElementById(CONFIG.SELECTORS.R_INPUT);
        const currentR = rInput ? parseFloat(rInput.value) : 1;
        const baseScale = CONFIG.CANVAS.SCALE;

        currentResults.forEach(p => {
            if (!p.r || p.r === 0) return;

            const color = p.hit ? CONFIG.CANVAS.HIT_COLOR : CONFIG.CANVAS.MISS_COLOR;
            const xForCurrentR = (p.x / p.r) * currentR;
            const yForCurrentR = (p.y / p.r) * currentR;
            const xPixel = this.centerX + xForCurrentR * baseScale;
            const yPixel = this.centerY - yForCurrentR * baseScale;

            this.ctx.fillStyle = color;
            this.ctx.beginPath();
            this.ctx.arc(xPixel, yPixel, CONFIG.CANVAS.POINT_RADIUS, 0, Math.PI * 2);
            this.ctx.fill();
        });
    }
}

function selectX(x) {
    const select = document.getElementById(CONFIG.SELECTORS.X_SELECT);
    if (select) {
        for (let i = 0; i < select.options.length; i++) {
            if (parseFloat(select.options[i].value) === x) {
                select.selectedIndex = i;
                select.dispatchEvent(new Event('change', { bubbles: true }));
                break;
            }
        }
    }
}

function redrawCanvas() {
    if (window.coordinatePlane) {
        window.coordinatePlane.draw();
    }
}

function handleAjaxComplete() {
    redrawCanvas();
}

document.addEventListener('DOMContentLoaded', () => {
    const rInput = document.getElementById(CONFIG.SELECTORS.R_INPUT);
    if (rInput && !rInput.value) {
        rInput.value = '1';
        rInput.dispatchEvent(new Event('input', { bubbles: true }));
    }

    document.querySelectorAll('.x-button').forEach(btn => {
        btn.addEventListener('click', function() {
            const x = parseFloat(this.textContent);
            selectX(x);
        });
    });

    new FormValidator();
    window.coordinatePlane = new CoordinatePlane(CONFIG.SELECTORS.CANVAS);

    redrawCanvas();
});