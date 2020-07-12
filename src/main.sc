require: birds.csv
    var = $Birds

patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю)

theme: /
    state: start
        q!: * *start 
        script: 
            $session.score=0;
            $session.all_birds = $Birds;

        a: Привет. Со мной ты можешь научиться определять птиц по голосу.
        go: /  

    state: song
        script:
            if ($session.all_birds.length == 0) {
                $reactions.answer("Больше мне нечего тебе загадать. Возвращайся позже.");} 
                $session.next_bird.link = ""
            } else {
                var i = Math.floor(Math.random()*($session.all_birds.length - 1)) + 1;
                $session.next_bird = $session.all_birds[i];
                $session.next_bird.splice(i, 1);
            }
        audio: $session.next_bird.link
        
        state: right
            q: $session.next_bird.name
            a: Верно, это {{$session.next_bird.name}}. 
            script:
                $session.score=$session.score + 1;
                $reactions.answer("Угадано уже '{{$session.score}}' птиц. Молодец!");} 
            go:
        
        state: Help
            q: * $Help *
            a: Это {{$session.next_bird.name}}. 
        
        state: wrong
            q: *
            a: Неверно.
                
        state: Stop
            q: $Stop
            a: Было приятно сыграть.
        
    state: reset
        q!: (reset|* *start)
        script:
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        go!: /