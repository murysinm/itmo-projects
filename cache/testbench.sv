`define C1_NOP 4'b0000
`define C1_READ8 4'b0001
`define C1_READ16 4'b0010
`define C1_READ32 4'b0011
`define C1_INVALIDATE_LINE 4'b0100
`define C1_WRITE8 4'b0101
`define C1_WRITE16 4'b0110
`define C1_WRITE32 4'b0111
`define C1_RESPONSE 4'b1000

`define C2_NOP 2'b00
`define C2_READ_LINE 2'b01
`define C2_WRITE_LINE 2'b10
`define C2_RESPONSE 2'b11

`define MEM_SIZE 2**20
`define CACHE_SIZE 2**10
`define CACHE_LINE_SIZE 32
`define CACHE_LINE_COUNT 32
`define CACHE_WAY 2
`define CACHE_SETS_COUNT 16
`define CACHE_TAG_SIZE 11
`define CACHE_SET_SIZE 4  
`define CACHE_OFFSET_SIZE 5
`define CACHE_ADDR_SIZE 9 
`define ADDR1_BUS_SIZE 15 
`define ADDR2_BUS_SIZE 15
`define DATA1_BUS_SIZE 16
`define DATA2_BUS_SIZE 16
`define CTR1_BUS_SIZE 4
`define CTR2_BUS_SIZE 2

`define M 64
`define N 60
`define K 32

module MemoryController(
        inout [`DATA2_BUS_SIZE-1:0] dataBus, 
        inout [`CTR2_BUS_SIZE-1:0] controlBus, 
        input CLK, 
        input [`ADDR2_BUS_SIZE-1:0] addressBus,
        input M_DUMP,
        input RESET
        );

    wire CLK;
  
    wire[`DATA2_BUS_SIZE-1:0] dataBus; 
    reg[`DATA2_BUS_SIZE-1:0] dataRegister;
    reg[7:0] dataBuffer[`CACHE_LINE_SIZE-1:0]; // буфер, где будем хранить данные, которые нужно записать в память
    
    wire[`ADDR2_BUS_SIZE-1:0] addressBus; 
    reg[`ADDR2_BUS_SIZE-1:0] addressBuffer; // буфер для хранения адреса
    
    wire[`CTR2_BUS_SIZE-1:0] controlBus;
    reg[`CTR2_BUS_SIZE-1:0] controlRegister;
    
    reg[7:0] memory[`MEM_SIZE-1:0];
    int ticksCount; // количество тактов, прошедших с запуска/reset'a
    int currentCount; // вспомогательная переменная для task'а для ожидания тактов
    int i; // переменная для циклов

    integer SEED;

    // "подключаем" провода к соответствующим регистрам:
    assign dataBus = dataRegister; 
    assign controlBus = controlRegister;

    // task для того, чтобы пропустить/подождать несколько тактов
    task waitTicks(input int TicksToWait);
        currentCount = ticksCount;
        wait(ticksCount == currentCount + TicksToWait);
    endtask

    initial begin
        dataRegister = 16'bz;
        controlRegister = 2'bz;
        ticksCount = 0;
        //$monitor("%d", ticksCount);
    end

    initial begin
        SEED = 225526;   
        
        for (i = 0; i < `MEM_SIZE; i += 1) begin
            memory[i] = $random(SEED)>>16;  
        end

        //for (i = 0; i < 100; i += 1) begin
        //$display("[%d] %d", i, memory[i]);  
        //end
    end

    always @(posedge CLK) begin
        if (RESET == 1) begin
            SEED = 225526;   
            for (i = 0; i < `MEM_SIZE; i += 1) begin
                memory[i] = $random(SEED)>>16;  
            end
            ticksCount = 0;
            dataRegister = 16'bz;
            controlRegister = 2'bz;
        end else begin
            ticksCount++;
        end
        //$display("%d", ticksCount);    
    end

    // обрабатываем команды

    always @(posedge CLK) begin
        if (controlBus == `C2_WRITE_LINE) begin
            if (M_DUMP == 1) begin
                $display("WRITE_LINE: \nticks: %d, address: %d, controlBus: %d", ticksCount, addressBus, controlBus);
            end
            //$display("address: %b, command: %b, dataBus: %b", address, command, data);
            // здесь запоминаем адрес и данные, чтобы потом писать в память
            addressBuffer = addressBus; 

            // сколько порций данных нам нужно получить? нам передают 1 кэш линию по 16 бит за такт
            // (32B / 2B) = 16  => нужно получить 16 порций данных:
            for (i = 0; i < 16; i++) begin
                // сохраняем в буфер побайтово: 
                dataBuffer[2 * i] = dataBus[7:0];
                dataBuffer[2 * i + 1] = dataBus[15:8];
                @(posedge CLK);
            end

            // мы работаем уже 16 тактов, подождем еще 84, чтобы было 100 тактов - время ответа памяти
            waitTicks(84);
            
            // пишем данные в память
            // добавляем к адресу 5 бит, так как 5 бит - это cache offset или log2(cache_line_size)
            for (i = {addressBuffer, 5'b00000}; i <= {addressBuffer, 5'b11111}; i++) begin
                memory[i] = dataBuffer[i - {addressBuffer, 5'b00000}];
            end 

            // записали данные, нужно ответить кэшу
            @ (negedge CLK);
            controlRegister = `C2_RESPONSE;

            // теперь, через такт (чтобы кэш успел заметить ответ), отдадим управление шиной кэшу
            @ (negedge CLK);
            controlRegister = 2'bz;

        end else if (controlBus == `C2_READ_LINE) begin
            if (M_DUMP == 1) begin
                $display("READ_LINE: \nticks: %d, address: %d, controlBus: %d", ticksCount, addressBus, controlBus);
            end
            // запоминаем адрес
            addressBuffer = addressBus; 

            // ждем 100 тактов - время ответа памяти
            waitTicks(100);
            // кэш уже убрал свою команду с шины, поставим команду, что отвечаем ему
            @ (negedge CLK);
            // отвечаем кэшу
            controlRegister = `C2_RESPONSE;
            //dataRegister = {memory[{addressBuffer, 5'b00001}], memory[{addressBuffer, 5'b00000}]};
            //$display("%b", dataBus);
            for (i = {addressBuffer, 5'b00000}; i < {addressBuffer, 5'b11111}; i += 2) begin
                dataRegister = {memory[i + 1], memory[i]};
                @ (negedge CLK);
            end 

            // выполнили запрос, теперь нужно освободить шины
            controlRegister = 2'bz;
            dataRegister = 16'bz;
        end
    end

endmodule



module Cache(
        output [`ADDR2_BUS_SIZE-1:0] address2Bus,
        inout [`DATA1_BUS_SIZE-1:0] data1Bus, 
        inout [`CTR1_BUS_SIZE-1:0] control1Bus, 
        inout [`DATA2_BUS_SIZE-1:0] data2Bus, 
        inout [`CTR2_BUS_SIZE-1:0] control2Bus, 
        input CLK, 
        input [`ADDR1_BUS_SIZE-1:0] address1Bus,
        input C_DUMP,
        input RESET
        );
    wire CLK;
  
    wire[`DATA1_BUS_SIZE-1:0] data1Bus; 
    reg[`DATA1_BUS_SIZE-1:0] data1Register;
    wire[`DATA2_BUS_SIZE-1:0] data2Bus; 
    reg[`DATA2_BUS_SIZE-1:0] data2Register;
    reg[7:0] lineBuffer[`CACHE_LINE_SIZE-1:0];
    
    wire[`ADDR1_BUS_SIZE-1:0] address1Bus;
    reg[`CACHE_TAG_SIZE-1:0] tagBuffer;
    reg[`CACHE_SET_SIZE-1:0] setBuffer;
    reg[`CACHE_OFFSET_SIZE-1:0] offsetBuffer;
    wire[`ADDR2_BUS_SIZE-1:0] address2Bus;
    reg[`ADDR1_BUS_SIZE-1:0] address2Register;

    wire[`CTR1_BUS_SIZE-1:0] control1Bus;
    reg[`CTR1_BUS_SIZE-1:0] control1Register;
    wire[`CTR2_BUS_SIZE-1:0] control2Bus;
    reg[`CTR2_BUS_SIZE-1:0] control2Register;
    
    reg[7:0] cacheMemory[`CACHE_SETS_COUNT-1:0][`CACHE_WAY-1:0][`CACHE_LINE_SIZE-1:0]; // собственно, память кэша
    reg[`CACHE_TAG_SIZE-1:0] tags[`CACHE_SETS_COUNT-1:0][`CACHE_WAY-1:0]; // тэг для каждой линии в кэше 
    reg[`CACHE_SETS_COUNT-1:0] leastRecentlyUsed; // номер линии в блоке, которая была не использована дольше
    reg[`CACHE_WAY-1:0] isValid[`CACHE_SETS_COUNT-1:0]; // служебный бит valid, 0 или 1 для каждой кэш линии
    reg[`CACHE_WAY-1:0] isDirty[`CACHE_SETS_COUNT-1:0]; // служебный бит dirty, 0 или 1 для каждой кэш линии

    int ticksCount; // количество тактов, прошедших с запуска/reset'a
    int currentCount; // вспомогательная переменная для task'а для ожидания тактов
    int i, j; // переменные для циклов

    int hits;
    int sum;

    // переменная, в которой будем хранить номер линии в блоке в случае кэш попадания
    // -1 в случае кэш промаха
    int foundLine; 

    int bitCount;

    reg[31: 0] writeBuffer;
    
    // "подключаем" провода к соответствующим регистрам
    assign address2Bus = address2Register;
    assign data1Bus = data1Register;
    assign data2Bus = data2Register;
    assign control1Bus = control1Register;
    assign control2Bus = control2Register;

    always @(posedge CLK) begin
        if (RESET == 1) begin
            hits = 0;
            sum = 0;
            ticksCount = 0;
            data1Register = 16'bz;
            data2Register = 16'bz;
            address2Register = 15'bz;
            control1Register = 4'bz;
            control2Register = 2'bz;
            for (i = 0; i < `CACHE_SETS_COUNT; i++) begin
                leastRecentlyUsed[i] = 0;
                for (j = 0; j < `CACHE_WAY; j++) begin
                    isValid[i][j] = 0;
                    isDirty[i][j] = 0;
                end
            end
        end else begin
            ticksCount++;
        end
        // $display("ticks: %d, control1Bus: %d, control2Bus: %d", ticksCount, control1Bus, control2Bus);    
    end

    initial begin
        hits = 0;
        sum = 0;
        ticksCount = 0;
        data1Register = 16'bz;
        data2Register = 16'bz;
        address2Register = 15'bz;
        control1Register = 4'bz;
        control2Register = 2'bz;
        for (i = 0; i < `CACHE_SETS_COUNT; i++) begin
            leastRecentlyUsed[i] = 0;
            for (j = 0; j < `CACHE_WAY; j++) begin
                isValid[i][j] = 0;
                isDirty[i][j] = 0;
            end
        end
    end


    always @(C_DUMP) $display("HITS, sum: %d, %d", hits, sum);

    // task для того, чтобы пропустить/подождать несколько тактов
    task waitTicks(input int ticksToWait);
        currentCount = ticksCount;
        wait(ticksCount == currentCount + ticksToWait);
    endtask

    // по номеру блока в кэше (хранится в setBuffer) выкидывает линию в нем, которая дольше не использовалась
    task makeFreeSlotForLine;
        if (isDirty[setBuffer][leastRecentlyUsed[setBuffer]] == 1) begin
            // линия, которую хотим выкинуть, изменялась, и эти изменения не были записаны в память
            // запишем изменения в память
            // напрявляем команду памяти
            @ (negedge CLK);
            control2Register = `C2_WRITE_LINE;
            // передаем адрес
            address2Register = {tagBuffer, setBuffer};
            // передаем данные для записи
            // data2Register = {lineBuffer[0], lineBuffer[1]};
            for (j = 0; j < 16; j++) begin
                data2Register = {lineBuffer[2 * i + 1], lineBuffer[2 * i]};
                @ (negedge CLK);;
            end
            // освобождаем шину команд
            control2Register = 2'bz;
            // ждем ответа памяти
            wait(control2Bus == `C2_RESPONSE);
            // память ответила, всё записалось. освобождаем шины
            @ (negedge CLK);
            address2Register = 15'bz;
            data2Register = 16'bz;
            waitTicks(1); // ждем 1 такт, чтобы память освободила шину
        end
        // линия, которую мы хотим выкинуть, сохранена в памяти
        // поставим ей в бит valid 0
        isValid[setBuffer][leastRecentlyUsed[setBuffer]] = 0;
    endtask


    // достать из памяти линию, адрес которой лежит в буфере
    task getLineFromMemory;
        // заметим, что когда task вызвали, CLK был на posegde
        // нужно сделать запрос в память, чтобы получить линию по адресу
        // передаем команду и адрес
        @(negedge CLK);
        control2Register = `C2_READ_LINE;
        address2Register = {tagBuffer, setBuffer};
        if (C_DUMP == 1) begin
            //$display("tag: %b, set: %b", tagBuffer, setBuffer);
            //$display("getline: ticks: %d, control2: %b, address: %b", ticksCount, control2Register, address2Register);
        end
        // освобождаем шину
        // хотим писать, а пишем только на negedge:
        @(negedge CLK);
        control2Register = 2'bz;
        // ждем, пока память начнет отвечать
        wait(control2Bus == `C2_RESPONSE);
        @(posedge CLK);
        // память начала отвечать, сохраним данные в LineBuffer
        // дадут нам 16 порций данных (16 порций по 16 бит = 32 байта = CACHE_LINE_SIZE)
        for (i = 0; i < 16; i++) begin 
            lineBuffer[2 * i] = data2Bus[7:0];
            //$display("buffer: %b", lineBuffer[2 * i]);
            lineBuffer[2 * i + 1] = data2Bus[15:8];
            //$display("buffer: %b", lineBuffer[2 * i + 1]);
            @(posedge CLK);
        end
        //$display("got line");
        // взаимодействие с памятью завершено, освободим шину
        @(negedge CLK);
        address2Register = 15'bz;
        waitTicks(1); // ждем 1 такт, чтобы сигнал распространился
    endtask

    always @(posedge CLK) begin
        if (control1Bus == `C1_READ32 || control1Bus == `C1_READ16 || control1Bus == `C1_READ8) begin
            sum++;
            // запомним команду
            if (control1Bus == `C1_READ32) begin
                bitCount = 32;
            end else if (control1Bus == `C1_READ16) begin
                bitCount = 16;
            end else begin
                bitCount = 8;
            end
            if (C_DUMP == 1) begin
                $display("cache read: ticks: %d, control: %d, address: %d", ticksCount, control1Bus, address1Bus);
            end
            
            // сохраним передаваемый процессором адрес, он передается в два такта
            setBuffer = address1Bus[`CACHE_SET_SIZE-1:0];
            tagBuffer = address1Bus[`CACHE_SET_SIZE+`CACHE_TAG_SIZE-1:`CACHE_SET_SIZE];

            // ждем 1 такт
            @(posedge CLK);
            // сохраняем вторую половину адреса
            offsetBuffer = address1Bus[`CACHE_OFFSET_SIZE-1:0];
            // проверим, кэш попадание у нас или кэш промах
            // перебираем линии в наборе, соответствующем адресу
            foundLine = -1; // пока что считаем, что кэш промах
            for (i = 0; i < `CACHE_WAY; i++) begin
                // проверим, valid ли кэш линия, которую мы смотрим
                if (isValid[setBuffer][i] == 1) begin
                    // сравним tag линий:
                    //$display("tagBuffer: %b, tags[][]: %b", tagBuffer, tags[setBuffer][i]);
                    if (tagBuffer == tags[setBuffer][i]) foundLine = i;
                end    
            end
            if (foundLine == -1) begin
                if (C_DUMP) begin
                    $display("ticks: %d, cache miss", ticksCount);
                end
                // кэш-промах, ждем 4 такта перед тем, как делать запрос в память
                waitTicks(4);

                // идем в память за данными
                getLineFromMemory;

                // в lineBuffer лежит линия, сохраним ее в кэш                
                // нужно найти место. ищем:
                // сначала попробуем найти линию, в которой не хранятся данные (isValid 0)
                // тут идем с конца, чтобы в случае, когда invalid обе линии, взять первую
                for (i = `CACHE_WAY - 1; i >= 0; i--) begin
                    if (isValid[setBuffer][i] == 0) begin
                        foundLine = i;
                    end
                end 
                if (foundLine == -1) begin
                    if (C_DUMP == 1) begin
                        $display("all lines are valid, invalidating a line");
                    end
                    // все линии заняты
                    // освободим линию
                    // нужно выкинуть из набора линию, которая не использовалась дольше
                    makeFreeSlotForLine;
                    // теперь в наборе есть свободная линия. это та, которая дольше не использовалась
                    foundLine =  leastRecentlyUsed[setBuffer];
                end
                // нашли линию, куда можно записать данные. пишем:
                
                for (j = 0; j < `CACHE_LINE_SIZE; j++) begin
                    cacheMemory[setBuffer][foundLine][j] = lineBuffer[j];
                    //$display("setBuffer: %b, foundLine: %d, j: %d", setBuffer, foundLine, j);
                    //$display("lineBuffer: %b", lineBuffer[j]);
                    //$display("memory: %b", cacheMemory[setBuffer][foundLine][j]);
                end 
                // добавим служебные значения
                isValid[setBuffer][foundLine] = 1;
                isDirty[setBuffer][foundLine] = 0;
                tags[setBuffer][foundLine] = tagBuffer;
            end else begin
                hits++;
                //$display("wow! cache hit!");
                // кэш-попадание, ждем 6 тактов
                waitTicks(6);
            end

            // линия, откуда нам нужно взять данные, лежит в кэше
            // передаем данные
            @(negedge CLK);
            control1Register = `C1_RESPONSE;
            
            if (bitCount == 8) begin
                data1Register = {8'b0, cacheMemory[setBuffer][foundLine][offsetBuffer]};
                @(negedge CLK);
            end else begin
                for (i = 0; i < bitCount / 16; i++) begin
                    data1Register = {cacheMemory[setBuffer][foundLine][offsetBuffer + i + 1], cacheMemory[setBuffer][foundLine][offsetBuffer + i]};
                    @(negedge CLK);
                end
            end
            // команда выполнена, освободим шины
            data1Register = 16'bz;
            control1Register = 4'bz;

            // обратились к кэш линии, значит, дольше не обращались к другой
            leastRecentlyUsed[setBuffer] = ~foundLine;

        end else if (control1Bus == `C1_WRITE32 || control1Bus == `C1_WRITE16 || control1Bus == `C1_WRITE8) begin
            sum++;
            // запомним команду
            if (control1Bus == `C1_WRITE32) begin
                bitCount = 32;
            end else if (control1Bus == `C1_WRITE16) begin
                bitCount = 16;
            end else begin
                bitCount = 8;
            end
            if (C_DUMP == 1) begin
                $display("cache write: ticks: %d, control: %d, address: %d", ticksCount, control1Bus, address1Bus);
            end
            // сохраним адрес и данные, которые надо записать
            writeBuffer = data1Bus;
            setBuffer = address1Bus[`CACHE_SET_SIZE-1:0];
            tagBuffer = address1Bus[`CACHE_SET_SIZE+`CACHE_TAG_SIZE-1:`CACHE_SET_SIZE];
            // ждем 1 такт
            @(posedge CLK);
            // сохраняем вторую половину адреса
            offsetBuffer = address1Bus[`CACHE_OFFSET_SIZE-1:0];

            // если пишем 32 бита, то сохранить надо еще 16 бит
            if (bitCount == 32) writeBuffer[31:16] = data1Bus;


            // проверим, кэш попадание у нас или кэш промах
            // перебираем линии в наборе, соответствующем адресу
            foundLine = -1; // пока что считаем, что кэш промах
            for (i = 0; i < `CACHE_WAY; i++) begin
                // проверим, valid ли кэш линия, которую мы смотрим
                if (isValid[setBuffer][i] == 1) begin
                    // сравним tag линий:
                    if (tagBuffer == tags[setBuffer][i]) foundLine = i;
                end    
            end
            if (foundLine == -1) begin
                if (C_DUMP) begin
                    $display("ticks: %d, cache miss", ticksCount);
                end
                // кэш-промах, ждем 4 такта перед тем, как делать запрос в память
                waitTicks(4);

                // идем в память за данными
                getLineFromMemory;

                // в lineBuffer лежит линия, сохраним ее в кэш                
                // нужно найти место. ищем:
                // сначала попробуем найти линию, в которой не хранятся данные (isValid 0)
                // тут идем с конца, чтобы в случае, когда invalid обе линии, взять первую
                for (i = `CACHE_WAY - 1; i >= 0; i--) begin
                    if (isValid[setBuffer][i] == 0) begin
                        foundLine = i;
                    end
                end 
                if (foundLine == -1) begin
                    if (C_DUMP == 1) begin
                        $display("all lines are valid, invalidating a line");
                    end
                    // все линии заняты
                    // освободим линию
                    // нужно выкинуть из набора линию, которая не использовалась дольше
                    makeFreeSlotForLine;
                    // теперь в наборе есть свободная линия. это та, которая дольше не использовалась
                    foundLine =  leastRecentlyUsed[setBuffer];
                end
                // нашли линию, куда можно записать данные. пишем:
                
                for (j = 0; j < `CACHE_LINE_SIZE; j++) begin
                    cacheMemory[setBuffer][foundLine][j] = lineBuffer[j];
                    //$display("setBuffer: %b, foundLine: %d, j: %d", setBuffer, foundLine, j);
                    //$display("lineBuffer: %b", lineBuffer[j]);
                    //$display("memory: %b", cacheMemory[setBuffer][foundLine][j]);
                end 
                // добавим служебные значения
                isValid[setBuffer][foundLine] = 1;
                isDirty[setBuffer][foundLine] = 0;
                tags[setBuffer][foundLine] = tagBuffer;
            end else begin
                hits++;
                //$display("cache hit in write");
                // кэш-попадание, ждем 6 тактов
                waitTicks(6);
            end
            // линия, в которой мы будем изменять данные, лежит в кэше
            // меняем данные
            if (bitCount == 8) begin
                cacheMemory[setBuffer][foundLine][offsetBuffer] = writeBuffer[7:0];
            end else if (bitCount == 16) begin
                cacheMemory[setBuffer][foundLine][offsetBuffer] = writeBuffer[7:0];
                cacheMemory[setBuffer][foundLine][offsetBuffer + 1] = writeBuffer[15:8];
            end else begin
                cacheMemory[setBuffer][foundLine][offsetBuffer] = writeBuffer[7:0];
                cacheMemory[setBuffer][foundLine][offsetBuffer + 1] = writeBuffer[15:8];
                cacheMemory[setBuffer][foundLine][offsetBuffer + 2] = writeBuffer[23:16];
                cacheMemory[setBuffer][foundLine][offsetBuffer + 3] = writeBuffer[31:24];
            end

            @ (negedge CLK);
            control1Register = `C1_RESPONSE;
            // команда выполнена, освободим шину
            @ (negedge CLK);

            control1Register = 4'bz;

            // обратились к кэш линии, значит, дольше не обращались к другой
            leastRecentlyUsed[setBuffer] = ~foundLine;

        end else if (control1Register == `C1_INVALIDATE_LINE) begin
            // сохраним передаваемый процессором адрес, он передается в два такта
            setBuffer = address1Bus[`CACHE_SET_SIZE-1:0];
            tagBuffer = address1Bus[`CACHE_SET_SIZE+`CACHE_TAG_SIZE-1:`CACHE_SET_SIZE];

            // ждем 1 такт
            @(posedge CLK);
            // сохраняем вторую половину адреса
            offsetBuffer = address1Bus[`CACHE_OFFSET_SIZE-1:0];

            // нас просят инвалидировать линию по этому адресу
            if (isDirty[setBuffer][leastRecentlyUsed[setBuffer]] == 1) begin
                // линия, которую хотим выкинуть, изменялась, и эти изменения не были записаны в память
                // запишем изменения в память
                // напрявляем команду памяти
                @ (negedge CLK);
                control2Register = `C2_WRITE_LINE;
                // передаем адрес
                address2Register = {tagBuffer, setBuffer};
                // передаем данные для записи
                //data2Register = {lineBuffer[0], lineBuffer[1]};
                for (j = 0; j < 16; j++) begin
                    data2Register = {lineBuffer[2 * i + 1], lineBuffer[2 * i]};
                    @ (negedge CLK);;
                end
                // освобождаем шину команд
                control2Register = 2'bz;
                // ждем ответа памяти
                wait(control2Bus == `C2_RESPONSE);
                // память ответила, всё записалось. освобождаем шины
                @ (negedge CLK);
                address2Register = 15'bz;
                data2Register = 16'bz;
                waitTicks(1); // ждем 1 такт, чтобы память освободила шину
            end
            // линия, которую мы хотим выкинуть, сохранена в памяти
            // поставим ей в бит valid 0
            isValid[setBuffer][leastRecentlyUsed[setBuffer]] = 0;
        end
    end


endmodule

/*
    C2_NOP - "00"
    C2_READ_LINE - "01"
    C2_WRITE_LINE - "10"
    C2_RESPONSE - "11"
*/

module tb;
  	reg CLK;	
    reg RESET;
    reg M_DUMP;
    reg C_DUMP;
    wire[`ADDR2_BUS_SIZE-1:0] address2Bus;
    wire[`DATA2_BUS_SIZE-1:0] data2Bus;
    wire[`CTR2_BUS_SIZE-1:0] control2Bus;
    MemoryController memctr(data2Bus, control2Bus, CLK, address2Bus, M_DUMP, RESET);
    wire[`DATA1_BUS_SIZE-1:0] data1Bus;
    wire[`ADDR1_BUS_SIZE-1:0] address1Bus;
    wire[`CTR1_BUS_SIZE-1:0] control1Bus;
    Cache cache(address2Bus, data1Bus, control1Bus, data2Bus, control2Bus, CLK, address1Bus, C_DUMP, RESET);
    
    reg[`DATA1_BUS_SIZE-1:0] data1Register;
    reg[`ADDR1_BUS_SIZE-1:0] address1Register;
    reg[`CTR1_BUS_SIZE-1:0] control1Register;


    reg[31:0] dataBuffer;

    int ticksCount;
    int currentCount;

    reg[7:0] a[`M][`K];
    reg[15:0] b[`K][`N];
    reg[31:0] c[`M][`N];

    int pa;
    int pb;
    int pc;

    reg[7:0] q1;
    reg[15:0] q2;

    int y;
    int x;
    int k;
    int s;

    int i, j;

    assign data1Bus = data1Register;
    assign control1Bus = control1Register;
    assign address1Bus = address1Register;

    always @(posedge CLK) begin
        if (RESET == 1) begin
            CLK = 0;
            M_DUMP = 0;
            C_DUMP = 0;
            data1Register = 16'bz;
            control1Register = 4'bz;
            address1Register = 11'bz;
        end
        ticksCount++;    
    end

    task write8(input reg[19:0] address, input reg[7:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_WRITE8;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        // данные на запись
        data1Register = data;
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        //$display("GIVEN TAG : %b", address[`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE+`CACHE_TAG_SIZE-1:`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE]);
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(negedge CLK);
        // все записалось, освобождаем шины
        address1Register = 11'bz;
        data1Register = 16'bz;
        @(posedge CLK);
    endtask

    task write16(input reg[19:0] address, input reg[15:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_WRITE16;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        // данные на запись
        data1Register = data;
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(negedge CLK);
        // все записалось, освобождаем шины
        address1Register = 11'bz;
        data1Register = 16'bz;
        @(posedge CLK);
    endtask

    task write32(input reg[19:0] address, input reg[31:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_WRITE32;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        // первая половина данных на запись
        data1Register = data[15:0];
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        // вторая половина данных на запись
        data1Register = data[31:16];
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(negedge CLK);
        // все записалось, освобождаем шины
        address1Register = 11'bz;
        data1Register = 16'bz;
        @(posedge CLK);
    endtask

    task read8(input reg[19:0] address, output reg[7:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_READ8;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(posedge CLK);
        data = data1Bus[7:0];
        @(negedge CLK);
        // освобождаем шины
        address1Register = 11'bz;
        @(posedge CLK);
    endtask

    task read16(input reg[19:0] address, output reg[15:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_READ16;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(posedge CLK);
        data = data1Bus;
        @(negedge CLK);
        // освобождаем шины
        address1Register = 11'bz;
        @(posedge CLK);
    endtask

    task read32(input reg[19:0] address, output reg[31:0] data);
        @(negedge CLK);
        // передаем команду кэшу
        control1Register = `C1_READ32;
        //передаем tag и set - это (`CACHE_TAG_SIZE + `CACHE_SET_SIZE) старших бит
        address1Register = address[`CACHE_TAG_SIZE+`CACHE_SET_SIZE+`CACHE_OFFSET_SIZE-1:`CACHE_OFFSET_SIZE];
        @(negedge CLK);
        // передаем вторую часть адреса - оффсет. это `CACHE_OFFSET_SIZE младших бит
        address1Register = address[`CACHE_OFFSET_SIZE-1:0];
        @(negedge CLK);
        control1Register = 4'bz;
        // передали команду, ждем, пока нам ответят
        wait(control1Bus == `C1_RESPONSE);
        @(posedge CLK);
        data[15:0] = data1Bus;
        @(posedge CLK);
        data[31:16] = data1Bus;
        @(negedge CLK);
        // освобождаем шины
        address1Register = 11'bz;
        @(posedge CLK);
    endtask

    task waitTicks(input int TicksToWait);
        currentCount = ticksCount;
        wait(ticksCount == currentCount + TicksToWait);
    endtask
    


    initial begin
        CLK = 0;
        M_DUMP = 0;
        C_DUMP = 0;
        data1Register = 16'bz;
        control1Register = 4'bz;
        address1Register = 11'bz;
        waitTicks(1);
        pa = 0;
        pc = `M * `K + 2 * `K * `N;
        for (y = 0; y < `M; y++) begin
            waitTicks(1);
            for (x = 0; x < `N; x++) begin
                waitTicks(1);
                pb = `M * `K;
                waitTicks(1);
                s = 0;
                waitTicks(1);
                for (k = 0; k < `K; k++) begin
                    waitTicks(1);
                    // s += pa[k] * pb[x];
                    // pa[k] <=> mem[pa + k]
                    // pb[x] <=> mem[pb + 2 * x]
                    read8(pa + k, q1); // q1 - это pa[k]
                    read16(pb + 2 * x, q2); // q2 - это pb[x]
                    s += q1 * q2; // s += pa[k] * pb[x];
                    waitTicks(5 + 1);
                    pb += 2 * `N;
                    waitTicks(1);

                    waitTicks(2);
                end
                // pc[x] <=> mem[pc + 4 * x]
                write32(pc + 4 * x, s);
                c[y][x] = s; // записываем значение, которое положили в массив для отладки
                waitTicks(2);
            end
            pa += `K;
            waitTicks(1);
            pc += 4 * `N;
            waitTicks(1);
            waitTicks(2);
        end
        @(negedge CLK);
        C_DUMP = 1;
        @(posedge CLK);
        $display("total ticks: %d", ticksCount); 
        for (i = 0; i < `M; i++) begin
            for (j = 0; j < `N; j++) begin
                $write("%h ", c[i][j]);
            end
            $write("\n");
        end
        $finish;
    end

    always #1 CLK = ~CLK;

endmodule
