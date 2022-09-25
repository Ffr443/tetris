public class main {
public static void main(String[] args) {
    initFields();

    while(!endOfGame){
        input();
        logic();

        graphicsModule.draw(gameField);
        graphicsModule.sync(FPS);
    }

    graphicsModule.destroy();
}
private static void input(){
    /// Обновляем данные модуля ввода
    keyboardModule.update();

    /// Считываем из модуля ввода направление для сдвига падающей фигурки
    shiftDirection = keyboardModule.getShiftDirection();

    /// Считываем из модуля ввода, хочет ли пользователь повернуть фигурку
    isRotateRequested = keyboardModule.wasRotateRequested();

    /// Считываем из модуля ввода, хочет ли пользователь "уронить" фигурку вниз
    isBoostRequested = keyboardModule.wasBoostRequested();

    /// Если был нажат ESC или "крестик" окна, завершаем игру
    endOfGame = endOfGame || keyboardModule.wasEscPressed() || graphicsModule.isCloseRequested();
}
public interface GraphicsModule {

    /**
     * Отрисовывает переданное игровое поле
     *
     * @param field Игровое поле, которое необходимо отрисовать
     */
    void draw(GameField field);

    /**
     * @return Возвращает true, если в окне нажат "крестик"
     */
    boolean isCloseRequested();

    /**
     * Заключительные действия, на случай, если модулю нужно подчистить за собой.
     */
    void destroy();

    /**
     * Заставляет программу немного поспать, если последний раз метод вызывался
     * менее чем 1/fps секунд назад
     */
    void sync(int fps);
}
public interface KeyboardHandleModule {

    /**
     * Считывание последних данных из стека событий, если модулю это необходимо
     */
    void update();

    /**
     * @return Возвращает информацию о том, был ли нажат ESCAPE за последнюю итерацию
     */
    boolean wasEscPressed();

    /**
     * @return Возвращает направление, в котором пользователь хочет сдвинуть фигуру.
     * Если пользователь не пытался сдвинуть фигуру, возвращает ShiftDirection.AWAITING.
     */
    ShiftDirection getShiftDirection();

    /**
     * @return Возвращает true, если пользователь хочет повернуть фигуру.
     */
    boolean wasRotateRequested();

    /**
     * @return Возвращает true, если пользователь хочет ускорить падение фигуры.
     */
    boolean wasBoostRequested();
}
private static void logic(){
    if(shiftDirection != ShiftDirection.AWAITING){ // Если есть запрос на сдвиг фигуры

       /* Пробуем сдвинуть */
       gameField.tryShiftFigure(shiftDirection);

       /* Ожидаем нового запроса */
       shiftDirection = ShiftDirection.AWAITING;
    }

    if(isRotateRequested){ // Если есть запрос на поворот фигуры

       /* Пробуем повернуть */
       gameField.tryRotateFigure();

       /* Ожидаем нового запроса */
       isRotateRequested = false;
    }

    /* Падение фигуры вниз происходит если loopNumber % FRAMES_PER_MOVE == 0
     * Т.е. 1 раз за FRAMES_PER_MOVE итераций.
     */
    if( (loopNumber % (FRAMES_PER_MOVE / (isBoostRequested ? BOOST_MULTIPLIER : 1)) ) == 0) gameField.letFallDown();

    /* Увеличение номера итерации (по модулю FPM)*/
    loopNumber = (loopNumber+1)% (FRAMES_PER_MOVE);
/* Если поле переполнено, игра закончена */
       endOfGame = endOfGame || gameField.isOverfilled();
    }
/** Флаг для завершения основного цикла программы */
    private static boolean endOfGame;

    /** Графический модуль игры*/
    private static GraphicsModule graphicsModule;

    /** "Клавиатурный" модуль игры, т.е. модуль для чтения запросов с клавиатуры*/
    private static KeyboardHandleModule keyboardModule;

    /** Игровое поле. См. документацию GameField */
    private static GameField gameField;

    /** Направление для сдвига, полученное за последнюю итерацию */
    private static ShiftDirection shiftDirection;

    /** Был ли за последнюю итерацию запрошен поворот фигуры */
    private static boolean isRotateRequested;

    /** Было ли за последнюю итерацию запрошено ускорение падения*/
    private static boolean isBoostRequested;

    /** Номер игровой итерации по модулю FRAMES_PER_MOVE.
     *  Падение фигуры вниз происходит если loopNumber % FRAMES_PER_MOVE == 0
     *  Т.е. 1 раз за FRAMES_PER_MOVE итераций.
     */
    private static int loopNumber;
private static void initFields() {
    loopNumber = 0;
    endOfGame = false;
    shiftDirection = ShiftDirection.AWAITING;
    isRotateRequested = false;
    graphicsModule = new LwjglGraphicsModule();
    keyboardModule = new LwjglKeyboardHandleModule();
    gameField = new GameField();
}
/**  Цвета ячеек поля. Для пустых ячеек используется константа EMPTINESS_COLOR */
    private TpReadableColor[][] theField;

    /** Количество непустых ячеек строки.
     *  Можно было бы получать динамически из theField, но это дольше.
     */
    private int[] countFilledCellsInLine;
/**  Информация о падающей в данный момент фигуре   */
private Figure figure;
public GameField(){
    spawnNewFigure();

    theField = new TpReadableColor[COUNT_CELLS_X][COUNT_CELLS_Y+OFFSET_TOP];
    countFilledCellsInLine = new int[COUNT_CELLS_Y+OFFSET_TOP];
/**
    * Создаёт новую фигуру в невидимой зоне
    * X-координата для генерации не должна быть ближе к правому краю,
    * чем максимальная ширина фигуры (MAX_FIGURE_WIDTH), чтобы влезть в экран
    */
    private void spawnNewFigure(){
        int randomX = new Random().nextInt(COUNT_CELLS_X - MAX_FIGURE_WIDTH);

        this.figure = new Figure(new Coord(randomX, COUNT_CELLS_Y + OFFSET_TOP - 1));
    }
public TpReadableColor getColor(int x, int y) {
    return theField[x][y];
}
public boolean isOverfilled(){
    for(int i = 0; i < OFFSET_TOP; i++){
        if(countFilledCellsInLine[COUNT_CELLS_Y+i] != 0) return true;
    }

    return false;
}
public void tryShiftFigure(ShiftDirection shiftDirection) {
    Coord[] shiftedCoords = figure.getShiftedCoords(shiftDirection);

    boolean canShift = true;

    for(Coord coord: shiftedCoords) {
        if((coord.y<0 || coord.y>=COUNT_CELLS_Y+OFFSET_TOP)
         ||(coord.x<0 || coord.x>=COUNT_CELLS_X)
         || ! isEmpty(coord.x, coord.y)){
            canShift = false;
        }
    }

    if(canShift){
        figure.shift(shiftDirection);
    }
}
Coord[] rotatedCoords = figure.getRotatedCoords();

    boolean canRotate = true;

    for(Coord coord: rotatedCoords) {
        if((coord.y<0 || coord.y>=COUNT_CELLS_Y+OFFSET_TOP)
                ||(coord.x<0 || coord.x>=COUNT_CELLS_X)
                ||! isEmpty(coord.x, coord.y)){
            canRotate = false;
        }
    }

    if(canRotate){
        figure.rotate();
        }
public void letFallDown() {
    Coord[] fallenCoords = figure.getFallenCoords();

    boolean canFall = true;

    for(Coord coord: fallenCoords) {
        if((coord.y<0 || coord.y>=COUNT_CELLS_Y+OFFSET_TOP)
                ||(coord.x<0 || coord.x>=COUNT_CELLS_X)
                ||! isEmpty(coord.x, coord.y)){
            canFall = false;
        }
    }

    if(canFall) {
        figure.fall();
} else {
    Coord[] figureCoords = figure.getCoords();

    /* Флаг, говорящий о том, что после будет необходимо сместить линии вниз
     * (т.е. какая-то линия была уничтожена)
     */
    boolean haveToShiftLinesDown = false;

    for(Coord coord: figureCoords) {
        theField[coord.x][coord.y] = figure.getColor();

        /* Увеличиваем информацию о количестве статичных блоков в линии*/
        countFilledCellsInLine[coord.y]++;

        /* Проверяем, полностью ли заполнена строка Y
         * Если заполнена полностью, устанавливаем  haveToShiftLinesDown в true
         */
        haveToShiftLinesDown = tryDestroyLine(coord.y) || haveToShiftLinesDown;
    }

    /* Если это необходимо, смещаем линии на образовавшееся пустое место */
    if(haveToShiftLinesDown) shiftLinesDown();

    /* Создаём новую фигуру взамен той, которую мы перенесли*/
    spawnNewFigure();
}
private boolean tryDestroyLine(int y) {
    if(countFilledCellsInLine[y] < COUNT_CELLS_X){
        return false;
    }

    for(int x = 0; x < COUNT_CELLS_X; x++){
        theField[x][y] = EMPTINESS_COLOR;
    }

    /* Не забываем обновить мета-информацию! */
    countFilledCellsInLine[y] = 0;

    return true;
}
private void shiftLinesDown() {

    /* Номер обнаруженной пустой линии (-1, если не обнаружена) */
    int fallTo = -1;

    /* Проверяем линии снизу вверх*/
    for(int y = 0; y < COUNT_CELLS_Y; y++){
        if(fallTo == -1){ //Если пустот ещё не обнаружено
            if(countFilledCellsInLine[y] == 0) fallTo = y; //...пытаемся обнаружить (._.)
        } else { //А если обнаружено
            if(countFilledCellsInLine[y] != 0){ // И текущую линию есть смысл сдвигать...

                /* Сдвигаем... */
                for(int x = 0; x < COUNT_CELLS_X; x++){
                    theField[x][fallTo] = theField[x][y];
                    theField[x][y] = EMPTINESS_COLOR;
                }

                /* Не забываем обновить мета-информацию*/
                countFilledCellsInLine[fallTo] = countFilledCellsInLine[y];
                countFilledCellsInLine[y] = 0;

                /*
                 * В любом случае линия сверху от предыдущей пустоты пустая.
                 * Если раньше она не была пустой, то сейчас мы её сместили вниз.
                 * Если раньше она была пустой, то и сейчас пустая -- мы её ещё не заполняли.
                 */
                fallTo++;
            }
        }
    }
}
public Figure getFigure() {
    return figure;
}
/**
     * Мнимая координата фигуры. По этой координате
     * через маску генерируются координаты реальных
     * блоков фигуры.
     */
    private Coord metaPointCoords;

    /**
     * Текущее состояние поворота фигуры.
     */
    private RotationMode currentRotation;

    /**
     * Форма фигуры.
     */
    private FigureForm form;
public enum RotationMode {
    /** Начальное положение */
    NORMAL(0),

    /** Положение, соответствующее повороту против часовой стрелки*/
    FLIP_CCW(1),

    /** Положение, соответствующее зеркальному отражению*/
    INVERT(2),

    /** Положение, соответствующее повороту по часовой стрелке (или трём поворотам против)*/
    FLIP_CW(3);



    /** Количество поворотов против часовой стрелки, необходимое для принятия положения*/
    private int number;

    /**
     * Конструктор.
     *
     * @param number Количество поворотов против часовой стрелки, необходимое для принятия положения
     */
    RotationMode(int number){
        this.number = number;
    }

    /** Хранит объекты enum'а. Индекс в массиве соответствует полю number.
     *  Для более удобной работы getNextRotationForm().
     */
    private static RotationMode[] rotationByNumber = {NORMAL, FLIP_CCW, INVERT, FLIP_CW};

    /**
     * Возвращает положение, образованое в результате поворота по часовой стрелке
     * из положения perviousRotation
     *
     * @param perviousRotation Положение из которого был совершён поворот
     * @return Положение, образованное в результате поворота
     */
    public static RotationMode getNextRotationFrom(RotationMode perviousRotation) {
        int newRotationIndex = (perviousRotation.number + 1) % rotationByNumber.length;
        return rotationByNumber[newRotationIndex];
    }
}
/**
     * Конструктор.
     * Состояние поворота по умолчанию: RotationMode.NORMAL
     * Форма задаётся случайная.
     *
     * @param metaPointCoords Мнимая координата фигуры. См. документацию одноимённого поля
     */
    public Figure(Coord metaPointCoords){
        this(metaPointCoords, RotationMode.NORMAL, FigureForm.getRandomForm());
    }

    public Figure(Coord metaPointCoords, RotationMode rotation, FigureForm form){
        this.metaPointCoords = metaPointCoords;
        this.currentRotation = rotation;
        this.form = form;
    }
    }
/**
     * @return Координаты реальных ячеек фигуры в текущем состоянии
     */
    public Coord[] getCoords(){
        return form.getMask().generateFigure(metaPointCoords, currentRotation);
    }

    /**
     * @return Координаты ячеек фигуры, как если бы
     * она была повёрнута проти часовой стрелки от текущего положения
     */
    public Coord[] getRotatedCoords(){
        return form.getMask().generateFigure(metaPointCoords, RotationMode.getNextRotationFrom(currentRotation));
    }

    /**
     * Поворачивает фигуру против часовой стрелки
     */
    public void rotate(){
        this.currentRotation = RotationMode.getNextRotationFrom(currentRotation);
    }

    /**
     * @param direction Направление сдвига
     * @return Координаты ячеек фигуры, как если бы
     * она была сдвинута в указано направлении
     */
    public Coord[] getShiftedCoords(ShiftDirection direction){
        Coord newFirstCell = null;

        switch (direction){
            case LEFT:
                newFirstCell = new Coord(metaPointCoords.x - 1, metaPointCoords.y);
                break;
            case RIGHT:
                newFirstCell = new Coord(metaPointCoords.x + 1, metaPointCoords.y);
                break;
            default:
                ErrorCatcher.wrongParameter("direction (for getShiftedCoords)", "Figure");
        }

        return form.getMask().generateFigure(newFirstCell, currentRotation);
    }

    /**
     * Меняет мнимую X-координату фигуры
     * для сдвига в указаном направлении
     *
     * @param direction Направление сдвига
     */
    public void shift(ShiftDirection direction){
        switch (direction){
            case LEFT:
                metaPointCoords.x--;
                break;
            case RIGHT:
                metaPointCoords.x++;
                break;
            default:
                ErrorCatcher.wrongParameter("direction (for shift)", "Figure");
        }
    }

    /**
     * @return Координаты ячеек фигуры, как если бы
     * она была сдвинута вниз на одну ячейку
     */
    public Coord[] getFallenCoords(){
        Coord newFirstCell = new Coord(metaPointCoords.x, metaPointCoords.y - 1);

        return form.getMask().generateFigure(newFirstCell, currentRotation);
    }

    /**
     * Меняет мнимую Y-координаты фигуры
     * для сдвига на одну ячейку вниз
     */
    public void fall(){
        metaPointCoords.y--;
    }
public TpReadableColor getColor() {
    return form.getColor();
}public enum FigureForm {

    I_FORM (CoordMask.I_FORM, TpReadableColor.BLUE),
    J_FORM (CoordMask.J_FORM, TpReadableColor.ORANGE);

/** Маска координат (задаёт геометрическую форму) */
    private CoordMask mask;

    /** Цвет, характерный для этой формы */
    private TpReadableColor color;

    FigureForm(CoordMask mask, TpReadableColor color){
        this.mask = mask;
        this.color = color;
    }/**
     * Массив со всеми объектами этого enum'а (для удобной реализации getRandomForm() )
     */
    private static final FigureForm[] formByNumber = {I_FORM, J_FORM, L_FORM, O_FORM, S_FORM, Z_FORM, T_FORM,};

    /**
     * @return Маска координат данной формы
     */
    public CoordMask getMask(){
        return this.mask;
    }

    /**
     * @return Цвет, специфичный для этой формы
     */
    public TpReadableColor getColor(){
        return this.color;
    }

    /**
     * @return Случайный объект этого enum'а, т.е. случайная форма
     */
    public static FigureForm getRandomForm() {
        int formNumber = new Random().nextInt(formByNumber.length);
        return formByNumber[formNumber];
    }
/**
 * Каждая маска -- шаблон, который по мнимой координате фигуры и
 * состоянию её поворота возвращает 4 координаты реальных блоков
 * фигуры, которые должны отображаться.
 * Т.е. маска задаёт геометрическую форму фигуры.
 *
 * @author DoKel
 * @version 1.0
 */
public enum CoordMask {
    I_FORM(
            new GenerationDelegate() {
                @Override
                public Coord[] generateFigure(Coord initialCoord, RotationMode rotation) {
                    Coord[] ret = new Coord[4];

                    switch (rotation){
                        case NORMAL:
                        case INVERT:
                            ret[0] = initialCoord;
                            ret[1] = new Coord(initialCoord.x , initialCoord.y - 1);
                            ret[2] = new Coord(initialCoord.x, initialCoord.y - 2);
                            ret[3] = new Coord(initialCoord.x, initialCoord.y - 3);
                            break;
                        case FLIP_CCW:
                        case FLIP_CW:
                            ret[0] = initialCoord;
                            ret[1] = new Coord(initialCoord.x + 1, initialCoord.y);
                            ret[2] = new Coord(initialCoord.x + 2, initialCoord.y);
                            ret[3] = new Coord(initialCoord.x + 3, initialCoord.y);
                            break;
                    }

                    return ret;
                }
            }
    ),
    J_FORM(
            new GenerationDelegate() {
                @Override
                public Coord[] generateFigure(Coord initialCoord, RotationMode rotation) {
                    Coord[] ret = new Coord[4];

                    switch (rotation){
                        case NORMAL:
                            ret[0] = new Coord(initialCoord.x + 1 , initialCoord.y);
                            ret[1] = new Coord(initialCoord.x + 1, initialCoord.y - 1);
                            ret[2] = new Coord(initialCoord.x + 1, initialCoord.y - 2);
                            ret[3] = new Coord(initialCoord.x, initialCoord.y - 2);
                            break;
                        case INVERT:
                            ret[0] = new Coord(initialCoord.x + 1 , initialCoord.y);
                            ret[1] = initialCoord;
                            ret[2] = new Coord(initialCoord.x, initialCoord.y - 1);
                            ret[3] = new Coord(initialCoord.x, initialCoord.y - 2);
                            break;
                        case FLIP_CCW:
                            ret[0] = initialCoord;
                            ret[1] = new Coord(initialCoord.x + 1, initialCoord.y);
                            ret[2] = new Coord(initialCoord.x + 2, initialCoord.y);
                            ret[3] = new Coord(initialCoord.x + 2, initialCoord.y - 1);
                            break;
                        case FLIP_CW:
                            ret[0] = initialCoord;
                            ret[1] = new Coord(initialCoord.x, initialCoord.y - 1);
                            ret[2] = new Coord(initialCoord.x + 1, initialCoord.y - 1);
                            ret[3] = new Coord(initialCoord.x + 2, initialCoord.y - 1);
                            break;
                    }

                    return ret;
                }
            }
    );

/**
     * Делегат, содержащий метод,
     * который должен определять алгоритм для generateFigure()
     */
    private interface GenerationDelegate{

        /**
         * По мнимой координате фигуры и состоянию её поворота
         * возвращает 4 координаты реальных блоков фигуры, которые должны отображаться
         *
         * @param initialCoord Мнимая координата
         * @param rotation Состояние поворота
         * @return 4 реальные координаты
         */
        Coord[] generateFigure(Coord initialCoord,  RotationMode rotation);
    }

    private GenerationDelegate forms;

    CoordMask(GenerationDelegate forms){
        this.forms = forms;
    }

    /**
     * По мнимой координате фигуры и состоянию её поворота
     * возвращает 4 координаты реальных блоков фигуры, которые должны отображаться.
     *
     * Запрос передаётся делегату, спецефичному для каждого объекта enum'а.
     *
     * @param initialCoord Мнимая координата
     * @param rotation Состояние поворота
     * @return 4 реальные координаты
     */
    public Coord[] generateFigure(Coord initialCoord, RotationMode rotation){
        return this.forms.generateFigure(initialCoord, rotation);
    }

}



}
